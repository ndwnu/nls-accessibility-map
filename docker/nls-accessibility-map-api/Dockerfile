FROM ndwnls.azurecr.io/docker.io/library/eclipse-temurin:21-jre-jammy@sha256:eebd356ad7358b7094758e5787a6726f332917cfd56feab6457c56dab895cdbf AS builder
WORKDIR /builder
COPY application.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM ndwnls.azurecr.io/docker.io/library/eclipse-temurin:21-jre-jammy@sha256:eebd356ad7358b7094758e5787a6726f332917cfd56feab6457c56dab895cdbf

# Add non-privileged user
RUN useradd -u 1000 java

WORKDIR /application

COPY --chown=java:java --from=builder /builder/extracted/dependencies/ ./
COPY --chown=java:java --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --chown=java:java --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --chown=java:java --from=builder /builder/extracted/application/ ./

USER 1000

ENTRYPOINT ["sh", "-c", "java -jar application.jar"]
