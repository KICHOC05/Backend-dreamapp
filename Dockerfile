FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN chmod +x gradlew && ./gradlew clean shadowJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/build/libs/sleep-analysis-dreamapp-api-1.0-SNAPSHOT.jar app.jar
COPY config/server.docker.properties config/server.properties
RUN groupadd --system dreamapp && useradd --system --gid dreamapp --home-dir /app dreamapp \
    && chown -R dreamapp:dreamapp /app
USER dreamapp
EXPOSE 10000
CMD ["java", "-jar", "app.jar"]
