FROM registry.ndw.nu/docker-hub/library/eclipse-temurin:17-focal as builder
WORKDIR application
COPY application.jar application.jar

RUN java -Djarmode=layertools -jar application.jar extract

FROM registry.ndw.nu/docker-hub/library/eclipse-temurin:17-focal

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

COPY --chown=java:java logback-deploy.xml logback-deploy.xml

USER 1000

ENTRYPOINT java org.springframework.boot.loader.JarLauncher
