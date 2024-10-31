# Requirements

- You must have docker and docker-compose installed locally on your machine.
- In order to run the component test you need to run all dependencies locally. Go to project root and execute
  `make start-infra`.

# Things to know

- Graph hopper
    - The graph hopper network build by the component test will be logged as geojson in [.debug](.debug) folder and can
      be loaded into QGIS.
    - Output of the graph hopper network can be found in the expected location: [graphhopper](../../graphhopper)
- Traffic signs
    - The traffic signs mocked in the component test will be logged as geojson in [.debug](.debug) folder and can be
      loaded into QGIS.
- Output of jobs is places in the normal locations and the output of the job will be written there as
  well. [map-generation-destination](../../map-generation-destination)