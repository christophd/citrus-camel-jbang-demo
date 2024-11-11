import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables;
import static org.citrusframework.actions.ExecuteSQLAction.Builder.sql;
import static org.citrusframework.camel.dsl.CamelSupport.camel;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers;

public class HttpToPostgreSQLTest implements Runnable {

    @CitrusResource
    GherkinTestActionRunner t;

    @Override
    public void run() {
        t.given(
            createVariables()
                    .variable("id", "citrus:randomNumber(4)")
                    .variable("headline", "Camel rocks!")
        );

        t.given(
            testcontainers()
                    .postgreSQL()
                    .start()
                    .initScript(Resources.create("db.init.sql"))
                    .autoRemove(true)
        );

        t.given(
            camel().jbang()
                    .run()
                    .autoRemove(true)
                    .withSystemProperties(Resources.create("application.properties"))
                    .integrationName("http-to-postgresql")
                    .integration(Resources.create("HttpToPostgreSQL.java"))
        );

        t.when(
            http().client("http://localhost:8080")
                .send()
                .post("/headline")
                .message()
                .body("""
                { "id": ${id}, "headline": "${headline}" }
                """)
                .contentType("application/json")
        );

        t.then(
            http().client("http://localhost:8080")
                .receive()
                .response(HttpStatus.OK)
                .message().body("Headline created!")
        );

        t.then(
            sql()
                .query()
                .statement("SELECT headline FROM headlines WHERE ID=${id}")
                .validate("HEADLINE", "${headline}")
        );
    }

}
