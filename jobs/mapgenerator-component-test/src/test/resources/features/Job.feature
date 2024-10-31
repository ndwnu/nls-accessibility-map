Feature: Schedule events

  Scenario: Job generate geojson
    Given a simple Graph Hopper network
    And traffic signs
      | startNodeId | endNodeId | fraction | rvvCode | directionType | windowTime |
      | 5           | 11        | 0.5      | C12     | FORTH         | window 1   |
      | 2           | 8         | 0.5      | C12     | BACK          | window 2   |
    When run MapGenerationJob for traffic sign C12 with start location at node 2
    Then we expect InnerCircleBlocked geojson
    And we expect InnerCircleBlockedPolygon geojson
