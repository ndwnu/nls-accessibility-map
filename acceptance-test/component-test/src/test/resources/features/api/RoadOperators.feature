Feature: Road operators

  Scenario Outline:  Find all
    When i request all road operators for <apiVersion>
    Then it should match all road operators
    Examples:
      | apiVersion |
      | v1 |
      | v2 |

