Feature: AwsS3Consumer

  Background:
    Given Disable auto removal of Testcontainers resources
    Given variables
      | aws.s3.bucketNameOrArn | citrus-camel-demo |
      | aws.s3.message         | Hello Camel! |
      | aws.s3.key             | hello.txt |

  Scenario: Create infrastructure
    # Start LocalStack container
    Given Enable service S3
    Given start LocalStack container

  Scenario: Send and verify AWS S3 message
    # Create AWS-S3 client
    Given New Camel context
    Given load to Camel registry amazonS3Client.groovy
    # Start Camel JBang integration
    Given Camel integration property file application.properties
    Given run Camel integration AwsS3Consumer.java
    # Upload S3 message
    Given Camel exchange message header CamelAwsS3Key="${aws.s3.key}"
    Given send Camel exchange to("aws2-s3://${aws.s3.bucketNameOrArn}?amazonS3Client=#amazonS3Client") with body: ${aws.s3.message}
    # Verify Camel integration received message
    Then Camel integration AwsS3Consumer.java should print Body: { "message": "${aws.s3.message}" }

  Scenario: Stop resources
    Given stop LocalStack container
