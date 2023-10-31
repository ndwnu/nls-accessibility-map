# Introduction
This project provides an API to determine accessible road sections for a given vehicle type.

## How to run/test locally
Run:
```shell
make start-infra
```
This will start the preconfigured Keycloak container.
Run the Spring Boot application in IntelliJ.

The following public endpoints will be available without authorization:
* [Swagger UI](http://localhost:8080/api-docs)
* [Health endpoint](http://localhost:8080/actuator/health)

## Authorizing Swagger UI in frontend
Use `client-id` and `client-secret` from `application-test.yml`.

## How to run Karate IT with service running in IntelliJ
Run:
```shell
make start-infra
```
Run the Spring Boot application in IntelliJ.
After the service is up, run `nu.ndw.nls.accessibilitymap.backend.NlsAccessibilityMapApiIT`.

## Build application and run IT tests via Maven
Make sure your Spring Boot application is not running in IntelliJ.
Run:
```shell
make integration-test
```

## Testing on staging
For testing purposes, a staging test account has been created in Keycloak called `sa-nls-staging-test`.
To get the password (`client-secret`) for this account, see the KeePass file in the `nls-wiki` project.
