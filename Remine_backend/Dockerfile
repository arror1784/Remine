# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk AS build
WORKDIR /src

# Warm the Gradle dependency cache in its own layer before copying sources,
# so a source-only change doesn't force a full dependency re-download.
COPY gradlew ./
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY common/build.gradle.kts common/
COPY client-openai/build.gradle.kts client-openai/
COPY migration/build.gradle.kts migration/
COPY auth/build.gradle.kts auth/
COPY user/build.gradle.kts user/
COPY notification/build.gradle.kts notification/
COPY message/build.gradle.kts message/
COPY call/build.gradle.kts call/
COPY memory/build.gradle.kts memory/
COPY activity/build.gradle.kts activity/
COPY family/build.gradle.kts family/
COPY app-api/build.gradle.kts app-api/
RUN chmod +x gradlew && ./gradlew :app-api:dependencies --no-daemon || true

COPY . .
RUN ./gradlew :app-api:bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

# curl is installed unconditionally so the compose healthcheck is deterministic
# regardless of what the base image happens to ship with.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Exact filename (not a glob) — Spring Boot's `application` plugin also produces
# a `-plain.jar` alongside the bootJar, and a glob would match both.
COPY --from=build /src/app-api/build/libs/app-api-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-Dspring.profiles.active=prod","-Duser.timezone=Asia/Seoul","-Djava.awt.headless=true","-XX:MaxRAMPercentage=70","-XX:MaxMetaspaceSize=128m","-jar","/app/app.jar"]
