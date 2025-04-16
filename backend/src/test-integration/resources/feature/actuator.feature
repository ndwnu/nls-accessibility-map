Feature: Health endpoint check

  Background:
    * url baseUrl

  Scenario: service health should be ok
    Given path '/actuator/health'
    When method GET
    Then status 200

  Scenario: service liveness should be ok
    Given path '/actuator/health/liveness'
    When method GET
    Then status 200

  Scenario: service readiness should be ok
    Given path '/actuator/health/readiness'
    When method GET
    Then status 200
