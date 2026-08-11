FROM eclipse-temurin:21-jdk-noble AS builder

WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew

COPY src ./src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew buildFatJar --no-daemon

FROM eclipse-temurin:21-jre-noble AS runtime

RUN groupadd --system app \
    && useradd \
        --system \
        --gid app \
        --home-dir /app \
        --shell /usr/sbin/nologin \
        app

WORKDIR /app

COPY --from=builder \
    --chown=app:app \
    /workspace/build/libs/app.jar /app/app.jar

USER app

ENV PORT=8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
