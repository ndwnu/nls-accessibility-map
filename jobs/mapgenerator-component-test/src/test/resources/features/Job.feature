Feature: Schedule events

  Scenario: Job generate geojson for window times
    Given a simple Graph Hopper network
    And with traffic signs for requested traffic sign types "C12"
      | startNodeId | endNodeId | fraction | rvvCode | directionType | windowTime | id                                   |
      | 5           | 11        | 0.5      | C12     | FORTH         | window 1   | 00000000-0000-4000-0000-000000000001 |
      | 2           | 8         | 0.5      | C12     | BACK          | window 2   | 00000000-0000-4000-0000-000000000002 |
    When run MapGenerationJob with configuration
      | exportName         | startNodeId | trafficSignTypes | exportTypes                           | includeOnlyWindowSigns | publishEvents | polygonMaxDistanceBetweenPoints |
      | InnerCircleBlocked | 2           | C12              | LINE_STRING_GEO_JSON,POLYGON_GEO_JSON | false                  | true          | 0.5                             |
    Then we expect InnerCircleBlocked geojson
    And we expect InnerCircleBlockedPolygon geojson

  Scenario: Job generate geojson for asymmetric traffic-sign detection should output only asymmetrically placed traffic signs
    Given a simple Graph Hopper network
    And with traffic signs for requested traffic sign types "C6,C7,C7B,C12,C22C"
      | startNodeId | endNodeId | fraction | rvvCode | directionType | windowTime | id                                   |
      | 5           | 11        | 0.5      | C12     | FORTH         | window 1   | 00000000-0000-4000-0000-000000000001 |
      | 2           | 8         | 0.5      | C12     | FORTH         | window 2   | 00000000-0000-4000-0000-000000000002 |
      | 1           | 6         | 0.0      | C7      | FORTH         | window 3   | 00000000-0000-4000-0000-000000000003 |
      | 6           | 1         | 1.0      | C7      | BACK          | window 4   | 00000000-0000-4000-0000-000000000004 |
    When run MapGenerationJob with configuration
      | exportName                              | startNodeId | trafficSignTypes   | exportTypes                       |
      | TruckRestrictionsAsymmetricTrafficSigns | 2           | C6,C7,C7B,C12,C22C | ASYMMETRIC_TRAFFIC_SIGNS_GEO_JSON |
    Then we expect TruckRestrictionsAsymmetricTrafficSigns geojson

  Scenario: Job generate issue export for asymmetric traffic-sign detection should send only asymmetrically placed traffic signs
    Given a simple Graph Hopper network
    And with traffic signs for requested traffic sign types "C6,C7,C7B,C12,C22C"
      | startNodeId | endNodeId | fraction | rvvCode | directionType | windowTime | id                                   |
      | 5           | 11        | 0.5      | C12     | FORTH         | window 1   | 00000000-0000-4000-0000-000000000001 |
      | 2           | 8         | 0.5      | C12     | FORTH         | window 2   | 00000000-0000-4000-0000-000000000002 |
      | 1           | 6         | 0.0      | C7      | FORTH         | window 3   | 00000000-0000-4000-0000-000000000003 |
      | 6           | 1         | 1.0      | C7      | BACK          | window 4   | 00000000-0000-4000-0000-000000000004 |
    And with issues sent to issue api
    When run MapGenerationJob with configuration
      | exportName                              | startNodeId | trafficSignTypes   | exportTypes                     |
      | TruckRestrictionsAsymmetricTrafficSigns | 2           | C6,C7,C7B,C12,C22C | ASYMMETRIC_TRAFFIC_SIGNS_ISSUES |
    Then we expect 2 issues to be created
