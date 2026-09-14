FROM eclipse-temurin:17-jdk@sha256:36d9a76dc231587873b103c68a789b85d91b41d314dda69730d6bc43a777f2a9 AS build
WORKDIR /src
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY src/ src/
COPY config/ config/
RUN ./gradlew clean shadowJar --no-daemon

FROM eclipse-temurin:17-jre@sha256:c6f2875c05ea10f16398bdc5f73405c384991506f2f1ead8bcc6582ae8adea79
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
