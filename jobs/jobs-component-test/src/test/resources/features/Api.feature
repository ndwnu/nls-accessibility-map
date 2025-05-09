Feature: Api

  Scenario: Emission zone
    Given a simple Graph Hopper network
    And graphHopper data is reloaded
    And with traffic signs
      | startNodeId | endNodeId | fraction | rvvCode | blackCode | directionType | emissionZoneId | id                                   |
      | 5           | 11        | 0.5      | C12     |           | FORTH         |                | 00000000-0000-4000-0000-000000000001 |
      | 6           | 1         | 0.9      | C18     | 1.9       | BACK          |                | 00000000-0000-4000-0000-000000000002 |
      | 7           | 8         | 0.1      | C22a    |           | FORTH         | zone-zero      | 00000000-0000-4000-0000-000000000003 |
      | 7           | 8         | 0.9      | C22a    |           | BACK          | zone-zero      | 00000000-0000-4000-0000-000000000004 |
      | 11          | 7         | 0.2      | C22a    |           | FORTH         | zone-low       | 00000000-0000-4000-0000-000000000005 |
      | 11          | 7         | 0.8      | C22a    |           | BACK          | zone-low       | 00000000-0000-4000-0000-000000000006 |
      | 3           | 4         | 0.1      | C19     | 1.9       | BACK          |                | 00000000-0000-4000-0000-000000000007 |
      | 3           | 4         | 0.9      | C19     | 1.9       | BACK          |                | 00000000-0000-4000-0000-000000000008 |
    When run TrafficSignUpdateCache
    And traffic signs data is reloaded
    When request accessibility for
      | startLatitude | startLongitude | municipalityId | vehicleType | fuelType | emissionClass | vehicleWidth |
      | 3             | 7              | GM0001         | truck       | diesel   | euro_3        | 2            |
    Then we expect the following blocked roadSections with matched roadSection with id 9 and is forward accessible true and backward accessible true
      | roadSectionId | forwardAccessible | backwardAccessible |
      | 6             | true              | false              |
      | 7             | false             | false              |
      | 11            | false             | false              |
      | 13            | false             | true               |
    When request accessibility geojson for
      | startLatitude | startLongitude | municipalityId | vehicleType | fuelType | emissionClass | vehicleWidth |
      | 3             | 7              | GM0001         | truck       | diesel   | euro_3        | 2            |
    Then we expect geojson to match response.emission-zone.geojson