# Introduction

This project is used for calculating road accessibility given certain traffic sign restrictions based on vehicle
characteristics.
It's main functional modules are a rest api service called backend and a jobs module subdivided into multiple more specialised submodules.

[Techincal details](docs/technical-details.md)

Module setup :

* Specification (API YAML definition)
* [Backend REST API](backend/README.md)
* accessibility shared core module
* Jobs module for background cron and event triggered jobs
  * graphhopper module to create a new graphhopper routable network on disk listens to nwb imported events
  * jobs-component-test module to run job's component tests
  * mapgenerator module to run various (geojson based) map generation routines
  * traffic-sign module for network analyses jobs and traffic sign cache updates
  * trafficsignclient module for accessing the traffic sign api to get the traffic sign data

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

## Building
Build Maven with profile `regression-test` to build the project with unit and integration testing.
If you want the component tests to run then also add `component-test`

## Build application and run IT tests via Maven
Make sure your Spring Boot application is not running in IntelliJ.
Run:
```shell
mvn verify -P regression-test -P component-test
```

## Testing on staging
For testing purposes, a staging test account has been created in Keycloak called `sa-nls-staging-test`.
To get the password (`client-secret`) for this account, see the KeePass file in the `nls-wiki` project.

## Versioning
All modules currently have the same version.


# Updating integration test data

## NDW Amersfoort sample data export
Set up a connection to the staging database and create an Amersfoort cut out in a temp table:
Make sure to update the ```version_id``` in the sql below when applicable. 
```sql
create table nwb.road_section_for_dump as 
    select * from nwb.road_section
             where version_id = 20240701 
               and geometry && st_makeenvelope(147769.55323, 455801.28125,163636.57098, 472114.299, 28992);
```
A table on staging should exist, and now we want to create a dump from it. The command below utilizes your local 
pg_dump executable (which might be located elsewhere on your computer). Since we already have the database schema, 
we only need the data. Use the following command to create the dump:

```shell
/usr/bin/pg_dump --dbname=nls-maps --schema=nwb --table=nwb.\"road_section_for_dump\" 
  --format=p --file=/tmp/road_sections_dump_ndw_20240101 --data-only 
  --username=nls_administrator --host=nls-postgres-staging.postgres.database.azure.com --port=5432
```
Since this is a data-only dump, we need to make sure to not completely override
`docker/nls-postgres/sql/10_nwb_schema.sql` and replace only the data part which starts with
`COPY nwb.road_section (version_id, road_section_id,`

After you replaced the data you also need to update the `version_id` and `reference_date` in the `nwb.version` 
table. You can find the `COPY nwb.version (version_id, imported, reference_date, revision, status) FROM stdin;` 
right below the added data.


## Traffic sign responses

When you update the traffig sign responses, you probably also need to update the NWB test data set version. Traffic sign
responses include `location.road.nwb_version` entries, of which the maximum value is used as reference date to look up 
the correct NWB version.

To re-create the mocked response, download Amersfoort files, using the following url:
https://data.ndw.nu/api/rest/static-road-data/traffic-signs/v4/current-state?townCode=GM0307&rvvCode=C6&rvvCode=C7&rvvCode=C7a&rvvCode=C7b&rvvCode=C8&rvvCode=C9&rvvCode=C10&rvvCode=C11&rvvCode=C12&rvvCode=C22c&rvvCode=C17&rvvCode=C18&rvvCode=C19&rvvCode=C20&rvvCode=C21

## Window Times

In the traffic sign API, traffic signs have text signs and some text signs are of type 'TIJD' and they have a field 
called 'openingHours'. This field will be using the OSM standard for opening hours: 
https://wiki.openstreetmap.org/wiki/Key:opening_hours

## Pretty printing geojson
jq . c6WindowTimeSegments.geojson | sponge c6WindowTimeSegments.geojson
