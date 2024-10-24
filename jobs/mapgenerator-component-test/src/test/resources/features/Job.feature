Feature: Schedule events

  Scenario: Job generate  event create
#		Given run container whoami
    Given Graph Hopper network
    And with traffic signs
#			| terminalID                           | threePhaseActivePower | fraction                     |
#			| 31000000-0000-0000-0000-000000000005 | 137.1                 | 0.5 |
#		Given run container in background nls-postgres
#		Given run container in background nls-rabbitmq
    Then run MapGenerationJob for traffic sign C12 in debug mode
#    Given run container nls-accessibility-map-generator-jobs in mode debug with environment variables
#      | key     | value                                                                                  |
#      | COMMAND | generateGeoJson --traffic-sign=C12 --include-only-time-windowed-signs --publish-events |
