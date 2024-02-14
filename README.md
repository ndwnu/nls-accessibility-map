# Introduction
This is a multi-module project for the Accessibility Map API. It consists of the following modules:

- Specification (API YAML definition)
- [Backend REST API](backend/README.md)
- Network generation job

## Structure
The specification module only versions the
[API YAML](specification/src/main/resources/nu/ndw/nls/accessibilitymap/specification/v1.yaml) and this JAR is used by
the `openapi-generator-maven-plugin` to generate controllers for the backend application.

## Building
Build Maven with profile `regression-test` to build the project with unit and integration testing.

## Build application and run IT tests via Maven
Make sure your Spring Boot application is not running in IntelliJ.
Run:
```shell
mvn verify -P regression-test
```

## Testing on staging
For testing purposes, a staging test account has been created in Keycloak called `sa-nls-staging-test`.
To get the password (`client-secret`) for this account, see the KeePass file in the `nls-wiki` project.

## Versioning
All modules currently have the same version.


# Updating integration test data

## NDW Amersfoort sample data export
Create Amersfoort cut out in temp table:
```sql
create table nwb.road_section_for_dump as 
    select * from nwb.road_section
             where version_id = 20231001 
               and geometry && st_makeenvelope(147769.55323, 455801.28125,163636.57098, 472114.299, 28992);
```

Export with pg_dump:
```shell
/usr/bin/pg_dump --dbname=nls-maps --schema=nwb --table=nwb.\"road_section_for_dump\" 
  --format=p --file=/tmp/road_sections_dump_ndw_20240101 --data-only 
  --username=nls_administrator --host=nls-postgres-staging.postgres.database.azure.com --port=5432
```

## Traffic sign responses

When you update the traffig sign responses, you probably also need to update the NWB test data set version. Traffic sign
responses include `location.road.nwb_version` entries, of which the maximum value is used as reference date to lookup 
the correct NWB version.

To re-create the mocked response, download Amersfoort files, using the following url:
https://data.ndw.nu/api/rest/static-road-data/traffic-signs/v3/current-state?town-code=GM0307&rvv-code=C6&rvv-code=C7&rvv-code=C7a&rvv-code=C7b&rvv-code=C8&rvv-code=C9&rvv-code=C10&rvv-code=C11&rvv-code=C12&rvv-code=C22c&rvv-code=C17&rvv-code=C18&rvv-code=C19&rvv-code=C20&rvv-code=C21

`driving_direction` was manually added to this mocked data and a patch `[driving_direction.patch](driving_direction.patch)
was created:
```shell
diff -Naur  original.json current-state.json > driving_direction.patch
```
that can be used to patch your response, assuming that the road section id of the used roads still exist.



