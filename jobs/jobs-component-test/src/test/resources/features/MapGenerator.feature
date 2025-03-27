Feature: Map Generator

  Scenario: Job generate geojson for window times
    Given a simple Graph Hopper network
    And with traffic signs
      | startNodeId | endNodeId | fraction | rvvCode | directionType | windowTime | id                                   |
      | 5           | 11        | 0.5      | C12     | FORTH         | window 1   | 00000000-0000-4000-0000-000000000001 |
      | 2           | 8         | 0.5      | C12     | BACK          | window 2   | 00000000-0000-4000-0000-000000000002 |
    When run MapGenerationJob with configuration
      | exportName         | startNodeId | trafficSignTypes | exportTypes                           | includeOnlyWindowSigns | publishEvents | polygonMaxDistanceBetweenPoints |
      | InnerCircleBlocked | 2           | C12              | LINE_STRING_GEO_JSON,POLYGON_GEO_JSON | false                  | true          | 0.5                             |
    Then we expect InnerCircleBlocked geojson
    And we expect InnerCircleBlockedPolygon geojson

  Scenario: Job generate geojson for window times with multiple traffic signs on one edge
    Given a simple Graph Hopper network
    And with traffic signs
      | startNodeId | endNodeId | fraction | rvvCode | directionType | windowTime | id                                   |
      | 5           | 11        | 0.5      | C12     | FORTH         | window 1   | 00000000-0000-4000-0000-000000000001 |
      | 2           | 8         | 0.5      | C12     | BACK          | window 2   | 00000000-0000-4000-0000-000000000002 |
      | 5           | 11        | 0.5      | C7      | FORTH         | window 3   | 00000000-0000-4000-0000-000000000003 |
      | 2           | 8         | 0.5      | C7      | BACK          | window 4   | 00000000-0000-4000-0000-000000000004 |
    When run MapGenerationJob with configuration
      | exportName                 | startNodeId | trafficSignTypes | exportTypes                           | includeOnlyWindowSigns | publishEvents | polygonMaxDistanceBetweenPoints |
      | InnerCircleBlockedMultiple | 2           | C12,C7           | LINE_STRING_GEO_JSON,POLYGON_GEO_JSON | false                  | false         | 0.5                             |
    Then we expect InnerCircleBlockedMultiple geojson
    And we expect InnerCircleBlockedMultiplePolygon geojson


  Scenario: Job generate geojson for asymmetric traffic-sign detection should output only asymmetrically placed traffic signs
    Given a simple Graph Hopper network
    And with traffic signs
      | startNodeId | endNodeId | fraction | rvvCode | directionType | windowTime | id                                   |
      | 5           | 11        | 0.5      | C12     | FORTH         | window 1   | 00000000-0000-4000-0000-000000000001 |
      | 2           | 8         | 0.5      | C12     | FORTH         | window 2   | 00000000-0000-4000-0000-000000000002 |
      | 1           | 6         | 0.0      | C7      | FORTH         | window 3   | 00000000-0000-4000-0000-000000000003 |
      | 6           | 1         | 1.0      | C7      | BACK          | window 4   | 00000000-0000-4000-0000-000000000004 |
    When run MapGenerationJob with configuration
      | exportName                              | startNodeId | trafficSignTypes   | exportTypes                       |
      | TruckRestrictionsAsymmetricTrafficSigns | 2           | C6,C7,C7B,C12,C22C | ASYMMETRIC_TRAFFIC_SIGNS_GEO_JSON |
    Then we expect TruckRestrictionsAsymmetricTrafficSigns geojson
