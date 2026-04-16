import java.net.URI;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.actions.testcontainers.aws2.AwsService;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.spi.Resources;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.aws2S3;

public class AwsS3ConsumerTest implements Runnable, TestActionSupport {

    @CitrusResource
    GherkinTestActionRunner t;

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
                .withOption("buckets", "${aws.s3.bucketNameOrArn}")
                .withService(AwsService.S3)
        );

        t.given(
            camel().jbang()
                    .run()
                    .integrationName("aws-s3-consumer")
                    .integration(Resources.create("AwsS3Consumer.java"))
                    .withSystemProperties(Resources.create("application.properties"))
        );

        t.then(
            camel()
                .send()
                .endpoint(aws2S3("${aws.s3.bucketNameOrArn}")
                            .advanced()
                            .amazonS3Client("#s3Client")::getRawUri)
                .message()
                .body("${aws.s3.message}")
                .header("CamelAwsS3Key", "${aws.s3.key}")
        );

        t.then(
            camel().jbang()
                    .verify()
                    .integration("aws-s3-consumer")
                    .waitForLogMessage("""
                        Body: { "message": "${aws.s3.message}" }
                        """)
        );
    }
}
