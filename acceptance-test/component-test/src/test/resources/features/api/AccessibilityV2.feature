Feature: Accessibility V2

  Scenario: Get - Vehicle width 2 meters - Destination reachable
    Given a simple Graph Hopper network
    And graphHopper data is reloaded
    And with traffic signs
      | startNodeId | endNodeId | fraction | rvvCode | blackCode | directionType | regulationOrderId | id                                   |
      | 5           | 11        | 0.5      | C12     |           | FORTH         |                   | 00000000-0000-4000-0000-000000000001 |
      | 6           | 1         | 0.9      | C18     | 1.9       | BACK          |                   | 00000000-0000-4000-0000-000000000002 |
      | 3           | 4         | 0.1      | C19     | 1.9       | BACK          |                   | 00000000-0000-4000-0000-000000000003 |
      | 3           | 4         | 0.9      | C19     | 1.9       | BACK          |                   | 00000000-0000-4000-0000-000000000004 |
    When run TrafficSignUpdateCache
    And traffic signs data is reloaded
    When request accessibility for truck2MetersWide-destination2-7
    Then we expect accessibility response truck2MetersWide-destination2-7
    When request accessibility geojson for truck2MetersWide-destination2-7
    Then we expect accessibility geojson response truck2MetersWide-destination2-7

    When request accessibility for truck2MetersWide-destination2-7-onlyAccessible
    Then we expect accessibility response truck2MetersWide-destination2-7-onlyAccessible
    When request accessibility geojson for truck2MetersWide-destination2-7-onlyAccessible
    Then we expect accessibility geojson response truck2MetersWide-destination2-7-onlyAccessible

    When request accessibility for truck2MetersWide-destination2-7-onlyInaccessible
    Then we expect accessibility response truck2MetersWide-destination2-7-onlyInaccessible
    When request accessibility geojson for truck2MetersWide-destination2-7-onlyInaccessible
    Then we expect accessibility geojson response truck2MetersWide-destination2-7-onlyInaccessible


  Scenario: Get - Vehicle emission class euro 3 - Destination unreachable
    Given a simple Graph Hopper network
    And graphHopper data is reloaded
    And with traffic signs
      | startNodeId | endNodeId | fraction | rvvCode | blackCode | directionType | regulationOrderId | id                                   |
      | 6           | 1         | 0.1      | C22a    |           | FORTH         | zone-zero         | 00000000-0000-4000-0000-000000000001 |
      | 6           | 1         | 0.9      | C22a    |           | BACK          | zone-zero         | 00000000-0000-4000-0000-000000000002 |
      | 1           | 2         | 0.1      | C22a    |           | FORTH         | zone-zero         | 00000000-0000-4000-0000-000000000003 |
      | 1           | 2         | 0.9      | C22a    |           | BACK          | zone-zero         | 00000000-0000-4000-0000-000000000004 |
    When run TrafficSignUpdateCache
    And traffic signs data is reloaded
    When request accessibility for truck-emissionEuro3-destination1-1
    Then we expect accessibility response truck-emissionEuro3-destination1-1-unreachable
    When request accessibility geojson for truck-emissionEuro3-destination1-1
    Then we expect accessibility geojson response truck-emissionEuro3-destination1-1-unreachable
