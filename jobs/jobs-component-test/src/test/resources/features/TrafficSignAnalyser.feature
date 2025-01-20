Feature: TrafficSignAnalyser

  Scenario: Job generate issue export for asymmetric traffic-sign detection should send only asymmetrically placed traffic signs
    Given a simple Graph Hopper network
    And with traffic signs for requested traffic sign types "C6"
      | startNodeId | endNodeId | fraction | rvvCode | directionType | windowTime | id                                   |
    And with traffic signs for requested traffic sign types "C7"
      | startNodeId | endNodeId | fraction | rvvCode | directionType | windowTime | id                                   |
      | 1           | 6         | 0.0      | C7      | FORTH         | window 3   | 00000000-0000-4000-0000-000000000003 |
      | 6           | 1         | 1.0      | C7      | BACK          | window 4   | 00000000-0000-4000-0000-000000000004 |
    And with traffic signs for requested traffic sign types "C12"
      | startNodeId | endNodeId | fraction | rvvCode | directionType | windowTime | id                                   |
      | 5           | 11        | 0.5      | C12     | FORTH         | window 1   | 00000000-0000-4000-0000-000000000001 |
      | 2           | 8         | 0.5      | C12     | FORTH         | window 2   | 00000000-0000-4000-0000-000000000002 |
    And with issues sent to issue api
    When run TrafficSignAnalyser with configuration
      | startNodeId | trafficSignTypes   | reportIssues |
      | 2           | C6,C7,C12 | true         |
    Then we expect the following issues to be reported
      | TrafficSign1-RoadSection12 |
      | TrafficSign2-RoadSection13 |
