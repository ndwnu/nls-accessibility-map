Feature: OpenAPI

  Background:
    * url baseUrl
    * def swaggerConfigResponse = read('classpath:test-messages/swagger-config-response.json')

  Scenario: apiYaml should return 200
    Given path '/api-docs/v1.yaml'
    When method GET
    Then status 200

  Scenario: swagger-config
    Given path '/api-docs/swagger-config'
    When method GET
    Then status 200
    And match response == swaggerConfigResponse

  Scenario: NDW Convention: Swagger UI is available at API base URI /api-docs Redirect
    SpringDoc will always do a 302 redirect when configuring another path. So we're accepting a 302 instead of 200.
    * configure followRedirects = false
    Given path '/api-docs'
    When method GET
    Then status 302

  Scenario: NDW Convention: Swagger UI is available at API base URI /api-docs. HTML page
    Given path '/api-docs'
    When method GET
    Then status 200
    And header Content-Type = 'text/html'
