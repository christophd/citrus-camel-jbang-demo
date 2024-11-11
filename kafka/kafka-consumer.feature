Feature: KafkaConsumer

  Background:
    Given Disable auto removal of Testcontainers resources
    Given Kafka consumer timeout is 5000 milliseconds
    Given Kafka topic: demo-topic

  Scenario: Start Kafka container
    Given start Kafka container

  Scenario: Send and verify Kafka message
    Given new Kafka connection
      | url | ${CITRUS_TESTCONTAINERS_KAFKA_BOOTSTRAP_SERVERS} |
    Given variables
      | key     | citrus:randomNumber(4) |
      | message | Hello Kafka |
    # Start Camel JBang integration
    Given Camel integration property file application.properties
    Given run Camel integration KafkaConsumer.java
    # Send Kafka message
    And Kafka message key: ${key}
    When send Kafka message with body and headers: ${message}
      | messageId | ${key} |
    # Verify Camel integration received message
    Then Camel integration KafkaConsumer.java should print ${message}

  Scenario: Stop resources
    Given stop Camel integration KafkaConsumer.java
    Given stop Kafka container
