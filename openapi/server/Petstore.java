package server;

import org.apache.camel.builder.RouteBuilder;

public class Petstore extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        restConfiguration()
                .clientRequestValidation(true)
                .apiContextPath("openapi");

        rest()
            .openApi()
            .specification("petstore-api.json");
    }
}
