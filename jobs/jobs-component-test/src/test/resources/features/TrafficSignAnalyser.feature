Feature: TrafficSignAnalyser

  Scenario: Job generate issue export for asymmetric traffic-sign detection should send only asymmetrically placed traffic signs
    Given a simple Graph Hopper network
    And with traffic signs for requested traffic sign types "C6"
      | startNodeId | endNodeId | fraction | rvvCode | directionType | windowTime | id |
    And with traffic signs for requested traffic sign types "C17"
      | startNodeId | endNodeId | fraction | rvvCode | blackCode | directionType | windowTime | id                                   |
      | 1           | 6         | 0.0      | C17     | invalid   | FORTH         | window 3   | 00000000-0000-4000-0000-000000000003 |
      | 6           | 1         | 1.0      | C17     | 1.9 m     | BACK          | window 4   | 00000000-0000-4000-0000-000000000004 |
    And with traffic signs for requested traffic sign types "C12"
      | startNodeId | endNodeId | fraction | rvvCode | directionType | windowTime | id                                   |
      | 5           | 11        | 0.5      | C12     | FORTH         | window 1   | 00000000-0000-4000-0000-000000000001 |
      | 2           | 8         | 0.5      | C12     | FORTH         | window 2   | 00000000-0000-4000-0000-000000000002 |
    And with issues sent to issue api
    When run TrafficSignAnalyser with configuration
      | startNodeId | trafficSignGroups | reportIssues |
      | 2           | C6,C17:C12        | true         |
    Then we expect the following issues to be reported
      | TrafficSign1-RoadSection13 |
      | TrafficSign2-RoadSection12 |
    Then we expect the report to be marked as completed for trafficSignTypes "C6,C17"
    And we expect the report to be marked as completed for trafficSignTypes "C12"