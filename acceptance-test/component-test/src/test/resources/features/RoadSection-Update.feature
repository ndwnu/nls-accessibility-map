Feature: Handle events to update road sections

  Background:
    Given no traffic signs

  Scenario: update road section for an active nwb version will result in roads getting blocked
    Given a simple network with nwb version 2026-03-01
    When a road section update event nwb-update-change-to-bike-path
    Then request accessibility geojson for truck2MetersWide-destination3-7 and expect result truck2MetersWide-destination3-7-bike-path-isInaccessible

  Scenario: update road section for an earlier nwb version will result in earlier message being ignored
    Given a simple network with nwb version 2026-03-01
    When an nwb version 2026-02-01
    And a road section update event nwb-update-earlier-map-version-to-one-way
    Then request accessibility geojson for truck2MetersWide-destination3-7 and expect result truck2MetersWide-destination3-7-road-change-update-not-processed
    When a road section update event nwb-update-change-to-bike-path
    Then request accessibility geojson for truck2MetersWide-destination3-7 and expect result truck2MetersWide-destination3-7-bike-path-isInaccessible

  Scenario: update road section for a later nwb version will result in exception and when a new nwb version is available the earlier message is ignored.
    Given a simple network with nwb version 2026-03-01
    When a road section update event nwb-update-later-map-version-to-bike-path
    And a road section update event nwb-update-change-to-roadway
    And a simple network with nwb version 2026-04-01
    Then request accessibility geojson for truck2MetersWide-destination3-7 and expect result truck2MetersWide-destination3-7-bike-path-isInaccessible

