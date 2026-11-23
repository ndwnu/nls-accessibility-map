FROM ndwnls.azurecr.io/docker.io/library/eclipse-temurin:25-jre-resolute@sha256:a0862ac8790c648f6b5db8a113b9990d24fbe8e65f3c4ba8d68b0302d270c1e6 AS builder
WORKDIR /builder
COPY application.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM ndwnls.azurecr.io/docker.io/library/eclipse-temurin:25-jre-resolute@sha256:a0862ac8790c648f6b5db8a113b9990d24fbe8e65f3c4ba8d68b0302d270c1e6

# Add non-privileged user (the Ubuntu 26.04 base image already has a user with UID 1000)
RUN userdel -r ubuntu && useradd -u 1000 java

WORKDIR /application

COPY --chown=java:java --from=builder /builder/extracted/dependencies/ ./
COPY --chown=java:java --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --chown=java:java --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --chown=java:java --from=builder /builder/extracted/application/ ./

USER 1000

ENTRYPOINT ["sh", "-c", "java -jar application.jar"]
