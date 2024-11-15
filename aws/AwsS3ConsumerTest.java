import java.net.URI;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.spi.Resources;
import org.citrusframework.testcontainers.aws2.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.aws2S3;
import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.camel.dsl.CamelSupport.camel;
import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers;

public class AwsS3ConsumerTest implements Runnable {

    @CitrusResource
    GherkinTestActionRunner t;

    @CitrusResource
    TestContext context;

    @Override
    public void run() {
        t.given(
            createVariables()
                .variable("aws.s3.bucketNameOrArn", "citrus-camel-demo")
                .variable("aws.s3.key", "hello.txt")
                .variable("aws.s3.message", "Hello Camel!")
        );

        t.given(
            testcontainers()
                .localstack()
                .start()
                .withService(LocalStackContainer.Service.S3)
                .autoRemove(true)
        );

        t.then(
            camel().camelContext().start()
        );

        t.given(
            camel().bind().component("amazonS3Client", createS3Client(context))
        );

        t.given(
            camel().jbang()
                    .run()
                    .autoRemove(true)
                    .integrationName("aws-s3-consumer")
                    .integration(Resources.create("AwsS3Consumer.java"))
                    .withSystemProperties(Resources.create("application.properties"))
        );

        t.then(
            camel()
                .send()
                .endpoint(aws2S3("${aws.s3.bucketNameOrArn}")
                            .advanced()
                            .amazonS3Client("#amazonS3Client")::getRawUri)
                .message()
                .body("${aws.s3.message}")
                .header("CamelAwsS3Key", "${aws.s3.key}")
        );

        t.then(
            camel().jbang()
                    .verify("aws-s3-consumer")
                    .waitForLogMessage("""
                        Body: { "message": "${aws.s3.message}" }
                        """)
        );
    }

    private S3Client createS3Client(TestContext context) {
        S3Client s3 = S3Client
                .builder()
                .endpointOverride(URI.create(context.getVariable("${CITRUS_TESTCONTAINERS_LOCALSTACK_S3_URL}")))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                context.getVariable("${CITRUS_TESTCONTAINERS_LOCALSTACK_ACCESS_KEY}"),
                                context.getVariable("${CITRUS_TESTCONTAINERS_LOCALSTACK_SECRET_KEY}"))
                ))
                .forcePathStyle(true)
                .region(Region.of(context.getVariable("${CITRUS_TESTCONTAINERS_LOCALSTACK_REGION}")))
                .build();

        s3.createBucket(b -> b.bucket(context.getVariable("${aws.s3.bucketNameOrArn}")));

        return s3;
    }
}
