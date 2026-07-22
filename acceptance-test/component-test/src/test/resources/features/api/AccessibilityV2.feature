Feature: Accessibility V2

  Scenario: Get - Vehicle width 2 meters - Destination reachable
    Given a simple network

    And with traffic sign conditions
      | name  | vehicleType                                                         | widthInM | heightInM |
      | C12   | bus,car,deliveryVan,moped,motorcycle,taxi,agriculturalVehicle,truck |          |           |
      | C18   |                                                                     | 1.9      |           |
      | C19   |                                                                     |          | 1.9       |
      | truck | truck                                                               |          |           |

    And with traffic signs
      | startNodeId | endNodeId | fraction | rvvCode | restrictions | exemptions | directionType | regulationOrderId | id                                   |
      | 5           | 11        | 0.5      | C12     | C12          |            | FORTH         |                   | 00000000-0000-4000-0000-000000000001 |
      | 6           | 1         | 0.9      | C18     | C18          |            | BACK          |                   | 00000000-0000-4000-0000-000000000002 |
      | 3           | 4         | 0.1      | C19     | C19          |            | BACK          |                   | 00000000-0000-4000-0000-000000000003 |
      | 3           | 4         | 0.9      | C19     | C19          |            | BACK          |                   | 00000000-0000-4000-0000-000000000004 |
      | 8           | 2         | 0.5      | C12     | C12          | truck      | BACK          |                   | 00000000-0000-4000-0000-000000000004 |
    And run TrafficSignUpdateCache
    And with speed limits
      | startNodeId | endNodeId | forwardAverageSpeedLimit | backwardAverageSpeedLimit |
      | 5           | 11        | 30                       | 20                        |
    And run SpeedLimitUpdateCache
    When request accessibility geojson for truck2MetersWide-destination3-7
    Then we expect accessibility geojson response truck2MetersWide-destination3-7

    When request accessibility geojson for truck2MetersWide-destination3-7-onlyAccessible
    Then we expect accessibility geojson response truck2MetersWide-destination3-7-onlyAccessible

    When request accessibility geojson for truck2MetersWide-destination3-7-onlyInaccessible
    Then we expect accessibility geojson response truck2MetersWide-destination3-7-onlyInaccessible


  Scenario: Get - Dynamic restriction on node should only block edges with the same road section id
    Given a simple network
    When request accessibility geojson for dynamicRestrictionOnNode
    Then we expect accessibility geojson response dynamicRestrictionOnNode

  Scenario: Get - Vehicle emission class euro 3 - Destination unreachable
    Given a simple network

    And with traffic signs
      | startNodeId | endNodeId | fraction | rvvCode | directionType | regulationOrderId | id                                   |
      | 6           | 1         | 0.1      | C22a    | FORTH         | zone-zero         | 00000000-0000-4000-0000-000000000001 |
      | 6           | 1         | 0.9      | C22a    | BACK          | zone-zero         | 00000000-0000-4000-0000-000000000002 |
      | 1           | 2         | 0.1      | C22a    | FORTH         | zone-zero         | 00000000-0000-4000-0000-000000000003 |
      | 1           | 2         | 0.9      | C22a    | BACK          | zone-zero         | 00000000-0000-4000-0000-000000000004 |
    And run TrafficSignUpdateCache
    When request accessibility geojson for truck-emissionEuro3-destination1-2-dynamicRestrictions
    Then we expect accessibility geojson response truck-emissionEuro3-destination1-2-unreachable

    When request accessibility geojson for truck-emissionEuro3-destination3-7-dynamicRestrictions
    Then we expect accessibility geojson response truck-emissionEuro3-destination3-7-unreachable

    When request accessibility geojson for truck-emissionEuro3-destination1-2-dynamicRestrictions-effectivelyAccessible
    Then we expect accessibility geojson response truck-emissionEuro3-destination1-2-unreachable-effectivelyAccessible


  Scenario: Get - Vehicle emission class euro 3 - Destination unreachable - road operator exemption url
    Given a simple network with road operator code WS14

    And with traffic signs
      | startNodeId | endNodeId | fraction | rvvCode | directionType | regulationOrderId | id                                   |
      | 6           | 1         | 0.1      | C22a    | FORTH         | zone-zero         | 00000000-0000-4000-0000-000000000001 |
      | 6           | 1         | 0.9      | C22a    | BACK          | zone-zero         | 00000000-0000-4000-0000-000000000002 |
      | 1           | 2         | 0.1      | C22a    | FORTH         | zone-zero         | 00000000-0000-4000-0000-000000000003 |
      | 1           | 2         | 0.9      | C22a    | BACK          | zone-zero         | 00000000-0000-4000-0000-000000000004 |
    And run TrafficSignUpdateCache
    When request accessibility geojson for truck-emissionEuro3-destination1-2-dynamicRestrictions
    Then we expect accessibility geojson response truck-emissionEuro3-destination1-2-unreachable-withExemptionUrl


  Scenario: Get - Vehicle emission class euro 3 - Destination effectively accessible
    Given a simple network
    And with traffic signs
      | startNodeId | endNodeId | fraction | rvvCode | directionType | regulationOrderId | id                                   |
      | 6           | 1         | 0.1      | C22a    | FORTH         | zone-zero         | 00000000-0000-4000-0000-000000000001 |
      | 6           | 1         | 0.9      | C22a    | BACK          | zone-zero         | 00000000-0000-4000-0000-000000000002 |
      | 1           | 2         | 0.1      | C22a    | FORTH         | zone-zero         | 00000000-0000-4000-0000-000000000003 |
    And run TrafficSignUpdateCache
    When request accessibility geojson for truck-emissionEuro3-destination1-2-dynamicRestrictions-effectivelyAccessible
    Then we expect accessibility geojson response truck-emissionEuro3-destination1-2-reachable-effectivelyAccessible
    When request accessibility geojson for truck-emissionEuro3-destination1-2-dynamicRestrictions
    Then we expect accessibility geojson response truck-emissionEuro3-destination1-2-unreachable-oneway


  Scenario: Get - Bounding box inner circle - Destination reachable
    Given a simple network
    When request accessibility geojson for boundingBox-destination3-7
    Then we expect accessibility geojson response boundingBox-destination3-7


  Scenario: Get - Bounding box inner circle - Custom restrictions - Destination unreachable
    Given a simple network
    When request accessibility geojson for boundingBox-destination3-7-unreachable
    Then we expect accessibility geojson response boundingBox-destination3-7-unreachable


  Scenario: Get - network with uni-directional roads and car inaccessible roads sections will not be included
    Given a simple network with uni-directional road sections and car inaccessible carriageway types
    When request accessibility geojson for truck2MetersWide-destination3-7
    Then we expect accessibility geojson response truck2MetersWide-destination3-7-network-has-missing-roads


  Scenario: Get - request with destination on footpath will be snapped to nearest car accessible road
    Given a simple network with uni-directional road sections and car inaccessible carriageway types
    When request accessibility geojson for truck2MetersWide-destination9-1
    Then we expect accessibility geojson response truck2MetersWide-destination9-1-snapped-to-2
