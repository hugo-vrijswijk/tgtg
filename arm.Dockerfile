ARG ARCH=
FROM ${ARCH}eclipse-temurin:17

WORKDIR /opt/app

ARG LOCAL_PATH
COPY ${LOCAL_PATH} tgtg.jar

ENTRYPOINT ["java", "-jar", "/opt/app/tgtg.jar"]
