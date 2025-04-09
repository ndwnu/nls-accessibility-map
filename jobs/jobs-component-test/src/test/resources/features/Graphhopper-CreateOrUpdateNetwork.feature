Feature: Graphhopper-CreateOrUpdateNetwork.feature

  Scenario: Create or update network
    Given a simpel nwb network
    When run GraphhopperJob RabbitMQ is configured
    And a nwb network imported event is triggerd
    And run GraphhopperJob createOrUpdateNetwork is executed
