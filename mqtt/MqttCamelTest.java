import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.spi.Resources;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.pahoMqtt5;
import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.camel.dsl.CamelSupport.camel;
import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers;

public class MqttCamelTest implements Runnable {

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
                .addPortBinding("1883:1883")
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
                                .brokerUrl("tcp://localhost:${CITRUS_TESTCONTAINERS_MQTT_PORT}")
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
                    .verify("mqtt-camel")
                    .waitForLogMessage("Warm temperature at 21")
        );

        t.then(
            camel()
                .send()
                .endpoint(pahoMqtt5("${mqtt.topic}")
                                .brokerUrl("tcp://localhost:${CITRUS_TESTCONTAINERS_MQTT_PORT}")
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
                    .verify("mqtt-camel")
                    .waitForLogMessage("Cold temperature at 7")
        );
    }

}
