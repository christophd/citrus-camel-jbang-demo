import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.kubernetes.ClusterType;
import org.citrusframework.spi.Resources;

public class KnativeSourceTest implements Runnable, TestActionSupport {

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
                    .integrationName("knative-source")
                    .integration(Resources.create("KnativeSource.java"))
                    .withSystemProperties(Resources.create("application.properties"))
                    .withSystemProperty("timer.message", "${timer.message}")
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
