ARG DOCKER_REGISTRY=registry-emea.app.corpintra.net/dockerhub
#FROM $DOCKER_REGISTRY/library/eclipse-temurin:21-jdk-alpine AS runner
FROM eclipse-temurin:25.0.2_10-jdk-alpine AS runner

RUN \
    --mount=type=cache,target=/var/cache/apk,sharing=locked \
    apk add \
        kafka-client-java
