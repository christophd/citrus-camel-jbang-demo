import java.util.Collections;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.kafka.message.KafkaMessage;
import org.citrusframework.spi.Resources;

public class KafkaConsumerTest implements Runnable, TestActionSupport {

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
        );

        t.when(
            camel().jbang()
                    .run()
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
                    .verify()
                    .integration("kafka-consumer")
                    .waitForLogMessage("${message}")
        );
    }

}
