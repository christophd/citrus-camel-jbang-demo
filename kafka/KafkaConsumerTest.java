import java.util.Collections;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.kafka.message.KafkaMessage;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.camel.dsl.CamelSupport.camel;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers;

public class KafkaConsumerTest implements Runnable {

    @CitrusResource
    GherkinTestActionRunner t;

    @Override
    public void run() {
        t.given(
            createVariables()
                .variable("key", "citrus:randomNumber(4)")
                .variable("message", "Hello Kafka")
        );

        t.given(
            testcontainers()
                .kafka()
                .start()
                .autoRemove(true)
        );

        t.when(
            camel().jbang()
                    .run()
                    .autoRemove(true)
                    .integrationName("kafka-consumer")
                    .integration(Resources.create("KafkaConsumer.java"))
                    .withSystemProperties(Resources.create("application.properties"))
        );

        t.then(
            send()
                .endpoint("kafka:demo-topic?server=${CITRUS_TESTCONTAINERS_KAFKA_BOOTSTRAP_SERVERS}")
                .message(new KafkaMessage("${message}", Collections.singletonMap("messageId", "${key}"))
                        .messageKey("${key}"))
        );

        t.then(
            camel().jbang()
                    .verify("kafka-consumer")
                    .waitForLogMessage("${message}")
        );
    }

}
