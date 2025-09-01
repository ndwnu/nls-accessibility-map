Feature: Open API Interface

	Scenario: validate / redirection
		When /api/rest/static-road-data/accessibility-map/api-docs is requested by GET
      Then I expect a 200 response with body openApi/index.html

	Scenario: validate open api documentation page is working
		When /api/rest/static-road-data/accessibility-map/swagger-ui/index.html is requested by GET
		Then I expect a 200 response with body openApi/index.html

	Scenario: Validate v1 specification can be downloaded
		When /api/rest/static-road-data/accessibility-map/api-docs/v1.yaml is requested by GET
		Then I expect a 200 response with body openApi/v1.yaml

	Scenario: Validate geojson-geometry specification can be downloaded
		When /api/rest/static-road-data/accessibility-map/api-docs/geojson-geometry.yaml is requested by GET
		Then I expect a 200 response with body openApi/geojson-geometry.yaml
