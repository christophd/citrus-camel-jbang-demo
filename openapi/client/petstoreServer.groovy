import org.citrusframework.http.server.HttpServer
import org.citrusframework.http.server.HttpServerBuilder
import org.springframework.http.HttpStatus

HttpServer petstoreServer = new HttpServerBuilder()
        .port(8080)
        .defaultStatus(HttpStatus.CREATED)
        .autoStart(true)
        .build()
petstoreServer.initialize()
context.getReferenceResolver().bind("petstoreServer", petstoreServer)
