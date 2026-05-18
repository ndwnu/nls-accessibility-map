Feature: Municipalities

  Scenario Outline:  Find all
    When i request all municipalities for <apiVersion>
    Then it should match all municipalities
    Examples:
      | apiVersion |
      | v1 |
      | v2 |

