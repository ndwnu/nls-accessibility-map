Feature: Handle events to update road sections

  Background:
    Given reset listener counts

  Scenario: update road section for an active nwb version will result in roads getting blocked
    Given a simple network with nwb version
      | versionId | versionDate |
      | 20260301  | 2026-03-01  |
    And a road section update event nwb-update-change-to-bike-path
    And message is processed and processed success count is 1 and rejected count is 0
    Given run TrafficSignUpdateCache
    When request accessibility geojson for truck2MetersWide-destination3-7
    Then we expect accessibility geojson response truck2MetersWide-destination3-7-bike-path-isInaccessible

  Scenario: update road section for an earlier nwb version will result in message being ignored
    Given a simple network with nwb version
      | versionId | versionDate |
      | 20260301  | 2026-03-01  |
    And a road section update event nwb-update-earlier-map-version
    Given a road section update event nwb-update-change-to-bike-path
    When message is processed and processed success count is 1 and rejected count is 1

  Scenario: continue processing after failure without messageId
    Given a road section update event broken2 with messageId null
    Given a road section update event hello-world1
    When message is processed and processed success count is 1 and rejected count is 1




