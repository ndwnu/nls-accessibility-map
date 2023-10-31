Feature: API operations

  Background:
    * url baseUrl
    * def validToken = karate.properties['auth.token.nls-accessibility-map-api-service-account']
    * def invalidToken = karate.properties['auth.token.nls-accessibility-map-api-invalid-token-test']
    * configure headers = call read('classpath:headers.js') { token: #(validToken)}

  Scenario: testEndpoint should return 200
    Given path '/v1/test/nwb'
    And method POST
    Then status 200

  Scenario: testEndpoint without token should return 401
    * configure headers = null
    Given path '/v1/test/nwb'
    And method POST
    Then status 401

  Scenario: testEndpoint with invalid token should return 403
    * configure headers = call read('classpath:headers.js') { token: #(invalidToken)}
    Given path '/v1/test/nwb'
    And method POST
    Then status 403
