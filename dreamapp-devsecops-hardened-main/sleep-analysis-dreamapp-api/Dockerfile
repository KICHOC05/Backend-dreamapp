FROM eclipse-temurin:21-jdk@sha256:85f00967bcc624fc19fa9c2cf124ea426a5363898e267141726f31f358c2e14b AS build
WORKDIR /src
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY src/ src/
COPY config/ config/
RUN ./gradlew clean shadowJar --no-daemon

FROM eclipse-temurin:21-jre@sha256:7a65df4b22d2de92d4e04056e884f3b9122d70b21e2847fd66084278bd0ce037
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
