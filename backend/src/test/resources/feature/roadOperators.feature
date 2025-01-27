Feature: Road operators endpoint

  Background:
    * url baseUrl
    * def okResponse = read('classpath:test-messages/roadOperators/response-ok.json')

  Scenario: Road operators - Find all
    Given path '/v1/road-operators'
    And method GET
    Then status 200
    And match response == okResponse
