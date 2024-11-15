Feature: PlatformHttpServer

  Background:
    Given URL: http://localhost:8080/hello
    Given variables
      | username | Christoph |

  Scenario: Invoke service
    # Start Camel JBang integration
    Given run Camel integration PlatformHttpServer.java
    # Send Http request
    And HTTP request query parameter name="${username}"
    When send GET
    # Verify response
    Then verify HTTP response body: Hello ${username}
    And receive HTTP 200 OK
