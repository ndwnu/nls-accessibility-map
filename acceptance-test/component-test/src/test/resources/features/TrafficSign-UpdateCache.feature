Feature: TrafficSign-UpdateCache

  Scenario: Update traffic sign cache
    Given a simple Graph Hopper network
    And with traffic signs
      | startNodeId | endNodeId | fraction | rvvCode | blackCode | directionType | windowTime | emissionZoneId | id                                   |
      | 5           | 11        | 0.5      | C12     |           | FORTH         | window 1   |                | 00000000-0000-4000-0000-000000000001 |
      | 1           | 6         | 0.0      | C17     | invalid   | FORTH         | window 3   |                | 00000000-0000-4000-0000-000000000002 |
      | 6           | 1         | 1.0      | C17     | 1.9       | BACK          | window 5   |                | 00000000-0000-4000-0000-000000000003 |
      | 7           | 8         | 0.5      | C22a    |           | BACK          |            | zone-zero      | 00000000-0000-4000-0000-000000000004 |
      | 7           | 8         | 0.5      | C22c    |           | BACK          |            | zone-low       | 00000000-0000-4000-0000-000000000005 |
    When run TrafficSignUpdateCache
    Then validate trafficSignCache
      | startNodeId | endNodeId | fraction | rvvCode | blackCode | directionType | windowTime | emissionZoneId | id                                   |
      | 5           | 11        | 0.5      | C12     |           | FORTH         | window 1   |                | 00000000-0000-4000-0000-000000000001 |
      | 6           | 1         | 1.0      | C17     | 190       | BACK          | window 5   |                | 00000000-0000-4000-0000-000000000003 |
      | 7           | 8         | 0.5      | C22a    | 190       | BACK          |            | zone-zero      | 00000000-0000-4000-0000-000000000004 |
      | 7           | 8         | 0.5      | C22c    | 190       | BACK          |            | zone-low       | 00000000-0000-4000-0000-000000000005 |