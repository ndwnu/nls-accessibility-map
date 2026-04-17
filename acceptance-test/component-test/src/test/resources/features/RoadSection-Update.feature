Feature: Handle events to update road sections

  Background:
    Given reset listener counts

  Scenario: update road section
    Given a road section update event hello-world
    When message is processed and processed success count is 1 and rejected count is 0

  Scenario: continue processing after failure
    Given a road section update event broken1 with messageId id-skippable-message
    Given a road section update event hello-world1
    When message is processed and processed success count is 1 and rejected count is 1

  Scenario: continue processing after failure without messageId
    Given a road section update event broken2 with messageId null
    Given a road section update event hello-world1
    When message is processed and processed success count is 1 and rejected count is 1




