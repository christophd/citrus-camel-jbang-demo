import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.kubernetes.ClusterType;
import org.citrusframework.spi.Resources;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables;
import static org.citrusframework.camel.dsl.CamelSupport.camel;
import static org.citrusframework.knative.actions.KnativeActionBuilder.knative;

public class KnativeSourceTest implements Runnable {

    @CitrusResource
    GherkinTestActionRunner t;

    private final ClusterType clusterType = ClusterType.LOCAL;

    @Override
    public void run() {
        t.given(
            createVariables()
                .variable("timer.message", "Hello Knative!")
        );

        t.given(
            knative()
                .brokers()
                .create("default")
                .clusterType(clusterType)
        );

        t.given(
            camel().jbang()
                    .run()
                    .autoRemove(true)
                    .integrationName("knative-source")
                    .integration(Resources.create("KnativeSource.java"))
                    .withSystemProperties(Resources.create("application.properties"))
                    .withSystemProperty("timer.message", "citrus:urlEncode(${timer.message})")
                    .withEnv("K_SINK", "http://localhost:8080")
        );

        t.then(
            knative()
                .event()
                .receive()
                .serviceName("default")
                .eventData("${timer.message}")
                .attribute("ce-id", "@notNull()@")
                .attribute("ce-type", "org.apache.camel.event.messages")
                .attribute("ce-source", "org.apache.camel")
                .attribute("Content-Type", "text/plain")
        );
    }
}
