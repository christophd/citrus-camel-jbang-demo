import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables;
import static org.citrusframework.camel.dsl.CamelSupport.camel;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

public class PlatformHttpServerTest implements Runnable {

    @CitrusResource
    GherkinTestActionRunner t;

    @Override
    public void run() {
        t.given(
            createVariables().variable("username", "Christoph")
        );

        t.given(
            camel().jbang()
                    .run()
                    .autoRemove(true)
                    .integrationName("platform-http-server")
                    .integration(Resources.create("PlatformHttpServer.java"))
        );

        t.when(
            http().client("http://localhost:8080")
                .send()
                .get("/hello")
                .queryParam("name", "${username}")
        );

        t.then(
            http().client("http://localhost:8080")
                .receive()
                .response(HttpStatus.OK)
                .message().body("Hello ${username}")
        );
    }

}
