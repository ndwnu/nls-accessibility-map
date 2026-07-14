dc = docker compose

.PHONY: help

help:                                ## Show this help.
	@fgrep -h "##" $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/##//'

stop:                                ## Stops the docker environment
	$(dc) down --remove-orphans

start-infra: 						 ## Starts specific containers needed for local environment --adapt when needed
	mkdir -p cache
	docker rmi nls-accessibility-map-initialise-cache-job:latest || true
	$(dc) up -d nls-keycloak nls-postgres accessibility-wiremock nls-rabbitmq nls-nwb-schema-manager initialise-cache-job

integration-test:                    ## Build and run it tests via maven
	mvn clean verify -Pregression-test

token-local:                         ## Create a token for local testing in swagger-ui
	./scripts/create-token-local.sh

publish-nwb-imported-event:
	./scripts/publish-event-nwb-imported.sh
