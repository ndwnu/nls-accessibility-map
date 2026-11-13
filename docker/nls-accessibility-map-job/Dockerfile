FROM ndwnls.azurecr.io/docker.io/library/eclipse-temurin:25-jre-jammy@sha256:a4e9fb35b48412cafbf036dabe201f22ee5e96f789e975bd6866ba0cbec45d17 AS builder
WORKDIR /builder
COPY application.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM ndwnls.azurecr.io/docker.io/library/eclipse-temurin:25-jre-jammy@sha256:a4e9fb35b48412cafbf036dabe201f22ee5e96f789e975bd6866ba0cbec45d17

# Add non-privileged user
RUN useradd -u 1000 java

WORKDIR /application

COPY --chown=java:java --from=builder /builder/extracted/dependencies/ ./
COPY --chown=java:java --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --chown=java:java --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --chown=java:java --from=builder /builder/extracted/application/ ./

USER 1000

ENTRYPOINT ["sh", "-c", "java -jar application.jar $COMMAND"]
