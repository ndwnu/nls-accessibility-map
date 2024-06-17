Feature: Municipalities endpoint

  Background:
    * url baseUrl
    * def municipalitiesOkResponse = read('classpath:test-messages/municipalities/response-ok.json')

  Scenario: municipalities should return 200
    Given path '/v1/municipalities'
    And method GET
    Then status 200
    And match response == municipalitiesOkResponse
