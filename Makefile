dc = docker-compose

.PHONY: help

help:                               ## Show this help.
	@fgrep -h "##" $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/##//'

login-acr:                           ## Login to azure container registry for the database test container
	az acr login -n ndwnls

stop:                                ## Stops the docker environment
	$(dc) down --remove-orphans

start-infra:
    ## Starts specific containers needed for local environment --adapt when needed
	$(dc) up -d nls-keycloak nls-postgres traffic-sign-api-stub nls-rabbitmq

integration-test:                     ## Build and run it tests via maven
	mvn clean verify -Pregression-test

token-local:                          ## Create a token for local testing in swagger-ui
	./scripts/create-token-local.sh

feign-test:
	curl -X POST "http://localhost:8080/v1/decode" \
    -H  "Authorization: Bearer <insert token>" \
    -H  "Content-type: application/json" \
    --data '<groupOfLocations/>'

validate-helm-chart-api:
	@sed -i "s/@docker.image.tag@/98789.98789/g" ./deploy/nls-accessibility-map-api/Chart.yaml
	helm install \
		--dry-run \
		-f ./deploy/nls-accessibility-map-api/values-global.yaml \
		-f ./deploy/nls-accessibility-map-api/values-staging.yaml \
		--set secretProviderClass.userAssignedIdentityID='dry-run' \
		--set secretProviderClass.tenantID='dry-run' \
		nls-accessibility-map-api \
		./deploy/nls-accessibility-map-api/
	@sed -i "s/98789.98789/@docker.image.tag@/g" ./deploy/nls-accessibility-map-api/Chart.yaml

validate-helm-chart-jobs:
	@sed -i "s/@docker.image.tag@/98789.98789/g" ./deploy/nls-accessibility-map-jobs/Chart.yaml
	helm install \
		--dry-run \
		-f ./deploy/nls-accessibility-map-jobs/values-global.yaml \
		-f ./deploy/nls-accessibility-map-jobs/values-staging.yaml \
		--set secretProviderClass.userAssignedIdentityID='dry-run' \
		--set secretProviderClass.tenantID='dry-run' \
		nls-accessibility-map-jobs \
		./deploy/nls-accessibility-map-jobs/
	@sed -i "s/98789.98789/@docker.image.tag@/g" ./deploy/nls-accessibility-map-jobs/Chart.yaml
