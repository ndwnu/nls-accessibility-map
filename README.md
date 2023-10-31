# Introduction
This is a multi-module project for the Accessibility Map API. It consists of the following modules:

- Specification (API YAML definition)
- [Backend REST API](backend/README.md)

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
