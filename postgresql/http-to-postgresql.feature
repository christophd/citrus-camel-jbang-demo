Feature: PostgreSQL Kamelet sink

  Background:
    Given Disable auto removal of Testcontainers resources
    Given variables
      | id         | citrus:randomNumber(4) |
      | headline   | Camel rocks!           |

  Scenario: Start PostgreSQL
    Given load database init script db.init.sql
    Then start PostgreSQL container

  Scenario: Create and verify Pipe
    # Start Camel JBang integration
    Given Camel integration property file application.properties
    Given run Camel integration HttpToPostgreSQL.java
    # Invoke Camel service endpoint
    Given URL: http://localhost:8080
    And HTTP request header ContentType="application/json"
    And HTTP request body
    """
    { "id": ${id}, "headline": "${headline}" }
    """
    When send POST /headline
    Then expect HTTP response body: Headline created!
    Then receive HTTP 200 OK
    # Verify entry in Postgres database
    Given SQL query max retry attempts: 10
    Then SQL query: SELECT headline FROM headlines WHERE ID=${id}
    And verify column HEADLINE=${headline}

  Scenario: Stop Camel JBang integration
    And stop PostgreSQL container
