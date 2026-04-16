package server;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;

public class PetstoreTest implements Runnable, TestActionSupport {

    @CitrusResource
    GherkinTestActionRunner t;

    @Override
    public void run() {
        t.given(
            createVariables()
                .variable("petId", "1000")
        );

        t.given(
            camel().jbang()
                    .run()
                    .integrationName("petstore")
                    .integration(Resources.create("Petstore.java"))
                    .addResource("petstore-api.json")
                    .withSystemProperties(Resources.create("application.properties"))
        );

        OpenApiSpecification petstoreApi = OpenApiSpecification.from("http://localhost:8080/openapi");

        t.then(
            openapi().specification(petstoreApi)
                    .client("http://localhost:8080")
                    .send("getPetById")
        );

        t.then(
            openapi().specification(petstoreApi)
                    .client("http://localhost:8080")
                    .receive("getPetById", HttpStatus.OK)
        );
    }
}
