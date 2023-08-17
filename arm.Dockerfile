ARG ARCH=
FROM ${ARCH}eclipse-temurin:17

WORKDIR /usr/src/app

ARG LOCAL_PATH

COPY ${LOCAL_PATH} tgtg.jar

ENTRYPOINT ["java", "-jar", "tgtg.jar"]
