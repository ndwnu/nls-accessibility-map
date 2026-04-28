Feature: Handle events to update road sections

  Background:
    Given reset listener counts

  Scenario: update road section for an active nwb version will result in roads getting blocked
    Given a simple network with nwb versions
      | versionId | versionDate | isCurrent |
      | 20260301  | 2026-03-01  | true      |
    And a road section update event nwb-update-change-to-bike-path
    And message is processed and processed success count is 1 and rejected count is 0
    Given run TrafficSignUpdateCache
    When request accessibility geojson for truck2MetersWide-destination3-7
    Then we expect accessibility geojson response truck2MetersWide-destination3-7-bike-path-isInaccessible

  Scenario: update road section for an earlier nwb version will result in message being ignored
    Given a simple network with nwb versions
      | versionId | versionDate | isCurrent |
      | 20260301  | 2026-03-01  | true      |
      | 20260201  | 2026-02-01  | false     |
    And a road section update event nwb-update-earlier-map-version
    And a road section update event nwb-update-change-to-bike-path
    Then message is processed and processed success count is 1 and rejected count is 1


  Scenario: update road section for a later nwb version will result in exception and when a new nwb version is available the messages are processed
    Given a simple network with nwb versions
      | versionId | versionDate | isCurrent |
      | 20260301  | 2026-03-01  | true      |
      | 20260401  | 2026-04-01  | false     |
    And a road section update event nwb-update-later-map-version
    And a road section update event nwb-update-change-to-bike-path
    When a simple network with nwb versions
      | versionId | versionDate | isCurrent |
      | 20260501  | 2026-04-01  | true      |
    Then message is processed and processed success count is 1 and rejected count is 1

