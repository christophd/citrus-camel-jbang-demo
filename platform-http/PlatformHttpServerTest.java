import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;

public class PlatformHttpServerTest implements Runnable, TestActionSupport {

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
