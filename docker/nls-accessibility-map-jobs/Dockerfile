FROM ndwnls.azurecr.io/docker.io/library/eclipse-temurin:17-focal as builder
WORKDIR application
COPY application.jar application.jar

RUN java -Djarmode=layertools -jar application.jar extract

FROM ndwnls.azurecr.io/docker.io/library/eclipse-temurin:17-focal

# Add non-privileged user
RUN useradd -u 1000 java

WORKDIR application

COPY --chown=java:java --from=builder application/dependencies/ ./
RUN true

COPY --chown=java:java --from=builder application/spring-boot-loader/ ./
RUN true

COPY --chown=java:java --from=builder application/snapshot-dependencies/ ./
RUN true

COPY --chown=java:java --from=builder application/application/ ./
RUN true

RUN mkdir ./graphhopper && chmod -R 755 ./graphhopper && chown -R java ./graphhopper
USER 1000

ENTRYPOINT java org.springframework.boot.loader.launch.JarLauncher $COMMAND
