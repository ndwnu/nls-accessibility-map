Feature: DataAnalyser-AnalyseBaseNetwork

  Scenario: Analyse base network
    Given a simple Graph Hopper network
    And NWB road sections
      | id   | junctionIdFrom | junctionIdTo |
      | 1000 | 2000           | 2001         |
    And with issues sent to issue api
    When run DataAnalyser RabbitMQ is configured
    And a network updated event is triggerd
    When run BaseNetworkAnalyser with configuration
      | startNodeId | reportIssues |
      | 2           | true         |
    Then we expect the following issues to be reported
      | UnreachableNetworkSegment-1000|
    Then we expect the report to be marked as completed for group UnreachableNetworkSegments