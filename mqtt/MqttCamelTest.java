import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.spi.Resources;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.pahoMqtt5;

public class MqttCamelTest implements Runnable, TestActionSupport {

    @CitrusResource
    GherkinTestActionRunner t;

    @Override
    public void run() {
        t.given(
            createVariables()
                .variable("mqtt.topic", "temperature")
                .variable("mqtt.client.id", "mqtt-citrus-client")
        );

        t.given(
            testcontainers()
                .container()
                .start()
                .image("eclipse-mosquitto:latest")
                .containerName("mqtt")
                .serviceName("mqtt")
                .addExposedPort(1883)
                .addPortBinding("13883:1883")
                .withVolumeMount("conf/", "/mosquitto/config")
        );

        t.when(
            camel().jbang()
                    .run()
                    .integrationName("mqtt-camel")
                    .integration(Resources.create("MqttCamel.java"))
                    .withSystemProperties(Resources.create("application.properties"))
        );

        t.then(
            camel().camelContext().start()
        );

        t.then(
            camel()
                .send()
                .endpoint(pahoMqtt5("${mqtt.topic}")
                                .brokerUrl("tcp://localhost:13883")
                                .clientId("${mqtt.client.id}")::getRawUri)
                .message()
                .body("""
                {
                  "value": 21
                }
                """)
        );

        t.then(
            camel().jbang()
                    .verify()
                    .integration("mqtt-camel")
                    .waitForLogMessage("Warm temperature at 21")
        );

        t.then(
            camel()
                .send()
                .endpoint(pahoMqtt5("${mqtt.topic}")
                                .brokerUrl("tcp://localhost:13883")
                                .clientId("${mqtt.client.id}")::getRawUri)
                .message()
                .body("""
                {
                  "value": 7
                }
                """)
        );

        t.then(
            camel().jbang()
                    .verify()
                    .integration("mqtt-camel")
                    .waitForLogMessage("Cold temperature at 7")
        );
    }

}
