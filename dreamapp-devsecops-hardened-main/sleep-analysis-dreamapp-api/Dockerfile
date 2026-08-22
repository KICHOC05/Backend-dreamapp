FROM eclipse-temurin:24-jdk@sha256:7493205ffe6caa8074fa8a06a276bb1c5ac41d3dd0fd43a0db66d7f776e80b3e AS build
WORKDIR /src
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY src/ src/
COPY config/ config/
RUN ./gradlew clean shadowJar --no-daemon

FROM eclipse-temurin:24-jre@sha256:8cb2387a28af84cf0db0948d9c67d4480192f4e567027a3963f145d218e8b4f2
WORKDIR /app
COPY --from=build /src/build/libs/sleep-analysis-dreamapp-api-1.0-SNAPSHOT-all.jar app.jar
COPY config/server.docker.properties.example config/server.properties
RUN apt-get update \
    && apt-get install --no-install-recommends -y curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system dreamapp && useradd --system --gid dreamapp --home-dir /app dreamapp \
    && chown -R dreamapp:dreamapp /app
USER dreamapp
EXPOSE 7070
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
    CMD curl --fail --silent --show-error http://127.0.0.1:7070/health || exit 1
ENTRYPOINT ["java", "-Xms128m", "-Xmx512m", "-XX:+UseG1GC", "-jar", "app.jar"]
