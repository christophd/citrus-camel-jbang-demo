package server;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables;
import static org.citrusframework.camel.dsl.CamelSupport.camel;
import static org.citrusframework.openapi.actions.OpenApiActionBuilder.openapi;

public class PetstoreTest implements Runnable {

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
                    .client("http://localhost:8080/petstore")
                    .send("getPetById")
        );

        t.then(
            openapi().specification(petstoreApi)
                    .client("http://localhost:8080/petstore")
                    .receive("getPetById", HttpStatus.OK)
        );
    }
}
