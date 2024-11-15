package client;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables;
import static org.citrusframework.actions.StartServerAction.Builder.start;
import static org.citrusframework.camel.dsl.CamelSupport.camel;
import static org.citrusframework.openapi.actions.OpenApiActionBuilder.openapi;

public class OpenApiClientTest implements Runnable {

    @CitrusResource
    GherkinTestActionRunner t;

    HttpServer petstoreServer = new HttpServerBuilder()
            .port(8080)
            .defaultStatus(HttpStatus.CREATED)
            .autoStart(true)
            .build();

    @Override
    public void run() {
        t.given(
            createVariables()
                .variable("petId", "1000")
        );

        petstoreServer.initialize();

        OpenApiSpecification petstoreApi = OpenApiSpecification.from("petstore-api.json");

        t.given(
            camel().jbang()
                    .run()
                    .integrationName("openapi-client")
                    .integration(Resources.create("OpenApiClient.java"))
                    .addResource("petstore-api.json")
                    .withSystemProperties(Resources.create("application.properties"))
        );

        t.then(
            openapi().specification(petstoreApi)
                    .server(petstoreServer)
                    .receive("addPet")
        );

        t.then(
            openapi().specification(petstoreApi)
                    .server(petstoreServer)
                    .send("addPet", HttpStatus.CREATED)
        );
    }
}
