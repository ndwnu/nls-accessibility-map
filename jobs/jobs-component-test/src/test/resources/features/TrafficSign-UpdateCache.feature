Feature: TrafficSign-UpdateCache

  Scenario: Update traffic sign cache
    Given a simple Graph Hopper network
    And with traffic signs
      | startNodeId | endNodeId | fraction | rvvCode | blackCode | directionType | windowTime | id                                   |
      | 5           | 11        | 0.5      | C12     |           | FORTH         | window 1   | 00000000-0000-4000-0000-000000000001 |
      | 1           | 6         | 0.0      | C17     | invalid   | FORTH         | window 3   | 00000000-0000-4000-0000-000000000002 |
      | 6           | 1         | 1.0      | C17     | 1.9       | BACK          | window 5   | 00000000-0000-4000-0000-000000000003 |
    When run TrafficSignUpdateCache
    Then validate trafficSignCache
      | startNodeId | endNodeId | fraction | rvvCode | blackCode | directionType | windowTime | id                                   |
      | 5           | 11        | 0.5      | C12     |           | FORTH         | window 1   | 00000000-0000-4000-0000-000000000001 |
      | 6           | 1         | 1.0      | C17     | 1.9       | BACK          | window 5   | 00000000-0000-4000-0000-000000000003 |