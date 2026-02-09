# Introduction

This project is used for calculating road accessibility given certain traffic sign restrictions based on vehicle
characteristics.
Its main functional modules are a rest api service called backend and a jobs module subdivided into multiple more specialised submodules.

[Techincal details](docs/technical-details.md)

Module setup :

* Specification (API YAML definition)
* [Backend REST API](backend/README.md)
* accessibility shared core module
* Job module for various jobs that need to be in the background.

## GitHub vs Azure DevOps
This project is maintained by Nationaal Dataportaal Wegverkeer (NDW)
* It is primarily maintained within Azure Devops and mirrored to https://github.com/ndwnu/nls-accessibility-map
* It works within NDW infrastructure, with some of its constraints;
* This repository contains functionality only, data is gathered from APIs/database, but not included;
* Pipelines are not designed to be used within GitHub;
* GitHub wiki and issues aren't enabled.

## Structure
The specification module only versions the
[API YAML](specification/src/main/resources/nu/ndw/nls/accessibilitymap/specification/v1.yaml) and this JAR is used by
the `openapi-generator-maven-plugin` to generate controllers for the backend application.

## Development
Execute `make start-infra`
Run the backend and the jobs through IntelliJ. Make sure you use at least the profiles `acceptance-test,dev`
Run any component test.


## Running with representative data locally
First run `make start-infra`. Then run the job `initializeCache` with profiles `acceptance-test,dev,job-initialize-cache,local-<your_name>-staging`. Put the staging credentials in `local-<your_name>-staging` profile. Example:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://nls-postgres-staging.postgres.database.azure.com:5432/nls-maps?ApplicationName=nls-accessibility-map-job&stringtype=unspecified
    username: nls
    password: <redacted>
```

## Window Times

In the traffic sign API, traffic signs have text signs and some text signs are of type 'TIJD' and they have a field 
called 'openingHours'. This field will be using the OSM standard for opening hours: 
https://wiki.openstreetmap.org/wiki/Key:opening_hours

## Pretty printing geojson
jq . c6WindowTimeSegments.geojson | sponge c6WindowTimeSegments.geojson
