FROM ndwnls.azurecr.io/docker.io/library/eclipse-temurin:25-jre-jammy@sha256:25777acfabf927084b7ef46d8bc786b6203c8c344f56541054238b8c4fe73db9 AS builder
WORKDIR /builder
COPY application.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM ndwnls.azurecr.io/docker.io/library/eclipse-temurin:25-jre-jammy@sha256:25777acfabf927084b7ef46d8bc786b6203c8c344f56541054238b8c4fe73db9

# Add non-privileged user
RUN useradd -u 1000 java

WORKDIR /application

COPY --chown=java:java --from=builder /builder/extracted/dependencies/ ./
COPY --chown=java:java --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --chown=java:java --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --chown=java:java --from=builder /builder/extracted/application/ ./

USER 1000

ENTRYPOINT ["sh", "-c", "java -jar application.jar $COMMAND"]
