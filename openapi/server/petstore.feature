Feature: Petstore

  Background:
    Given URL: http://localhost:8080/petstore
    Given variable petId="1000"

  Scenario: Invoke service
    # Start Camel JBang integration
    Given Camel integration property file application.properties
    Given Camel integration resource petstore-api.json
    Given run Camel integration Petstore.java
    # Send Http request
    Given OpenAPI specification: http://localhost:8080/openapi
    When invoke operation: getPetById
    # Verify response
    Then verify operation result: 200 OK

  Scenario: Stop Camel JBang integration
    Given stop Camel integration Petstore.java
