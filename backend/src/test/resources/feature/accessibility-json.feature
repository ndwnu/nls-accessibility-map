Feature: Inaccessible road sections JSON endpoint

  Background:
    * url baseUrl
    * def okResponse = read('classpath:test-messages/accessibility/json/response-ok.json')
    * def okResponseWithMatchedRoadSection  = read('classpath:test-messages/accessibility/json/response-ok-with-matched-road-section.json')
    * def badRequestMunicipalityId = read('classpath:test-messages/accessibility/response-400-incorrect-municipality-id.json')
    * def badRequestVehicleLength = read('classpath:test-messages/accessibility/response-400-incorrect-vehicle-length.json')
    * def badRequestHasTrailer = read('classpath:test-messages/accessibility/response-400-incorrect-has-trailer.json')
    * def badRequestLatitudeSetLongitudeMissing = read('classpath:test-messages/accessibility/response-400-longitude-missing.json')
    * def badRequestLongitudeSetLatitudeMissing = read('classpath:test-messages/accessibility/response-400-latitude-missing.json')

  Scenario: accessibility map without latitude and longitude should return 200
    Given path '/v1/municipalities/GM0307/road-sections'
    And param vehicleType = 'truck'
    And param vehicleLength = 5
    And param vehicleWidth = 3
    And param vehicleHeight = 2
    And param vehicleWeight = 4
    And param vehicleAxleLoad = 3
    And param vehicleHasTrailer = false
    And method GET
    Then status 200
    And match response == okResponse

  Scenario: accessibility map request with latitude and longitude specified should return 200
    Given path '/v1/municipalities/GM0307/road-sections'
    And param vehicleType = 'truck'
    And param vehicleLength = 5
    And param vehicleWidth = 3
    And param vehicleHeight = 2
    And param vehicleWeight = 4
    And param vehicleAxleLoad = 3
    And param vehicleHasTrailer = false
    And param latitude = 52.15551237
    And param longitude = 5.37886419
    And method GET
    Then status 200
    And match response == okResponseWithMatchedRoadSection

  Scenario: accessibility map request with latitude and longitude specified but no matching road section should return 200
    Given path '/v1/municipalities/GM0307/road-sections'
    And param vehicleType = 'truck'
    And param vehicleLength = 5
    And param vehicleWidth = 3
    And param vehicleHeight = 2
    And param vehicleWeight = 4
    And param vehicleAxleLoad = 3
    And param vehicleHasTrailer = false
    And param latitude = 52.1544069
    And param longitude = 5.3641687
    And method GET
    Then status 200
    And match response == okResponse

  Scenario: accessibility map with longitude set, but latitude missing should return 400
    Given path '/v1/municipalities/GM0307/road-sections'
    And param vehicleType = 'car'
    And param longitude = 1.0
    And method GET
    Then status 400
    And match response == badRequestLongitudeSetLatitudeMissing

  Scenario: accessibility map with latitude set, but longitude missing should return 400
    Given path '/v1/municipalities/GM0307/road-sections'
    And param vehicleType = 'car'
    And param latitude = 1.0
    And method GET
    Then status 400
    And match response == badRequestLatitudeSetLongitudeMissing

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
