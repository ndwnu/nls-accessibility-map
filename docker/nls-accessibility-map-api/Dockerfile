FROM ndwnls.azurecr.io/docker.io/library/eclipse-temurin:25-jre-jammy@sha256:7d1b8bf8b361b932f8c76589b963bc164e4fff416d832169f8dfe26c8aae8f50 AS builder
WORKDIR /builder
COPY application.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM ndwnls.azurecr.io/docker.io/library/eclipse-temurin:25-jre-jammy@sha256:7d1b8bf8b361b932f8c76589b963bc164e4fff416d832169f8dfe26c8aae8f50

# Add non-privileged user
RUN useradd -u 1000 java

WORKDIR /application

COPY --chown=java:java --from=builder /builder/extracted/dependencies/ ./
COPY --chown=java:java --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --chown=java:java --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --chown=java:java --from=builder /builder/extracted/application/ ./

USER 1000

ENTRYPOINT ["sh", "-c", "java -jar application.jar"]
