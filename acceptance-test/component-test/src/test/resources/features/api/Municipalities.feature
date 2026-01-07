Feature: Municipalities

  Scenario: Find all
    When i request all municipalities
    Then it should match all municipalities

