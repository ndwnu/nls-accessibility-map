Feature: DataAnalyser-AsymmetricTrafficSigns

  Scenario: Job generate issue export for asymmetric traffic-sign detection should send only asymmetrically placed traffic signs
    Given a simple Graph Hopper network
    And with traffic signs
      | startNodeId | endNodeId | fraction | rvvCode | blackCode | directionType | windowTime | id                                   |
      | 5           | 11        | 0.5      | C12     |           | FORTH         | window 1   | 00000000-0000-4000-0000-000000000001 |
      | 2           | 8         | 0.5      | C12     |           | FORTH         | window 2   | 00000000-0000-4000-0000-000000000002 |
      | 1           | 6         | 0.0      | C17     | invalid   | FORTH         | window 3   | 00000000-0000-4000-0000-000000000003 |
      | 6           | 1         | 1.0      | C17     | 1.9 m     | BACK          | window 4   | 00000000-0000-4000-0000-000000000004 |
    And with issues sent to issue api
    When run TrafficSignUpdateCache
    And run TrafficSignAnalyser with configuration
      | startNodeId | trafficSignGroups | reportIssues |
      | 2           | C6,C17:C12        | true         |
    Then we expect the following issues to be reported
      | TrafficSign1-RoadSection13 |
      | TrafficSign2-RoadSection12 |
    Then we expect the report to be marked as completed for trafficSignTypes "C12"