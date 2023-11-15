Feature: API operations

  Background:
    * url baseUrl
    * def validToken = karate.properties['auth.token.nls-accessibility-map-api-service-account']
    * def invalidToken = karate.properties['auth.token.nls-accessibility-map-api-invalid-token-test']
    * configure headers = call read('classpath:headers.js') { token: #(validToken)}

  Scenario: accessibility map should return 200
    Given path '/v1/municipalities/GM0344'
    And param vehicleType = 'Car'
    And method GET
    Then status 200

  Scenario: accessibility map without token should return 401
    * configure headers = null
    Given path '/v1/municipalities/GM0344'
    And param vehicleType = 'Car'
    And method GET
    Then status 401

  Scenario: accessibility map with invalid token should return 403
    * configure headers = call read('classpath:headers.js') { token: #(invalidToken)}
    Given path '/v1/municipalities/GM0344'
    And param vehicleType = 'Car'
    And method GET
    Then status 403
