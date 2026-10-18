FROM ndwnls.azurecr.io/docker.io/library/eclipse-temurin:25-jre-jammy@sha256:e0d8c47eccd7ec82d635542dfcd1f2b1293add6bdd7ffbeb650f8ec1b1335425 AS builder
WORKDIR /builder
COPY application.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM ndwnls.azurecr.io/docker.io/library/eclipse-temurin:25-jre-jammy@sha256:e0d8c47eccd7ec82d635542dfcd1f2b1293add6bdd7ffbeb650f8ec1b1335425

# Add non-privileged user
RUN useradd -u 1000 java

WORKDIR /application

COPY --chown=java:java --from=builder /builder/extracted/dependencies/ ./
COPY --chown=java:java --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --chown=java:java --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --chown=java:java --from=builder /builder/extracted/application/ ./

USER 1000

ENTRYPOINT ["sh", "-c", "java -jar application.jar $COMMAND"]
