Feature: Schedule events

  Scenario: Job generate  event create
    Given buildProject
    And Graph Hopper network
    And with traffic signs
    When run MapGenerationJob for traffic sign C12
    Then we expect the inner circle to be blocked
