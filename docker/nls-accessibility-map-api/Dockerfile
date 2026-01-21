FROM ndwnls.azurecr.io/docker.io/library/eclipse-temurin:21-jre-jammy as builder
WORKDIR application
COPY application.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM ndwnls.azurecr.io/docker.io/library/eclipse-temurin:21-jre-jammy

# Add non-privileged user
RUN useradd -u 1000 java

WORKDIR application

COPY --chown=java:java --from=builder application/dependencies/ ./
COPY --chown=java:java --from=builder application/spring-boot-loader/ ./
COPY --chown=java:java --from=builder application/snapshot-dependencies/ ./
COPY --chown=java:java --from=builder application/application/ ./

USER 1000

ENTRYPOINT java org.springframework.boot.loader.launch.JarLauncher
