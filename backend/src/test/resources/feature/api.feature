Feature: API operations

  Background:
    * url baseUrl
    * def okResponse = read('classpath:test-messages/accessibility/response-ok.json')
    * def badRequestMunicipalityId = read('classpath:test-messages/accessibility/response-400-incorrect-municipality-id.json')
    * def badRequestVehicleLength = read('classpath:test-messages/accessibility/response-400-incorrect-vehicle-length.json')
    * def badRequestHasTrailer = read('classpath:test-messages/accessibility/response-400-incorrect-has-trailer.json')

  Scenario: accessibility map should return 200
    Given path '/v1/municipalities/GM0307/road-sections'
    And param vehicleType = 'commercial_vehicle'
    And param vehicleLength = 5
    And param vehicleWidth = 3
    And param vehicleHeight = 2
    And param vehicleWeight = 4
    And param vehicleAxleLoad = 3
    And param vehicleHasTrailer = false
    And method GET
    Then status 200
    And match response == okResponse

  Scenario: accessibility map with invalid municipality id parameter value should return 400
    Given path '/v1/municipalities/GM000/road-sections'
    And param vehicleType = 'car'
    And method GET
    Then status 400
    And match response == badRequestMunicipalityId

  Scenario: accessibility map with invalid vehicleLength parameter value should return 400
    Given path '/v1/municipalities/GM0307/road-sections'
    And param vehicleType = 'car'
    And param vehicleLength = -5
    And method GET
    Then status 400
    And match response == badRequestVehicleLength

  Scenario: accessibility map with invalid vehicleHasTrailer parameter type should return 400
    Given path '/v1/municipalities/GM0307/road-sections'
    And param vehicleType = 'car'
    And param vehicleHasTrailer = 2
    And method GET
    Then status 400
    And match response == badRequestHasTrailer

  Scenario: municipalities should return 200
    Given path '/v1/municipalities'
    And method GET
    Then status 200
