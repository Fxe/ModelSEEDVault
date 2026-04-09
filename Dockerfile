# ---- Build stage ----
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /build

# Copy build descriptor files first — allows Docker to cache the dependency
# download layer separately from source changes.
COPY gradlew settings.gradle build.gradle ./
COPY gradle/ gradle/
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY src/ src/
RUN ./gradlew bootJar --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /build/build/libs/*.jar app.jar
COPY ./docker/vault-config.xml /app/vault-config.xml

# /app/graphdb — embedded Neo4j data directory (matches default vault.neo4j.path)
# /app/vault-config.xml — optional config override; mount to customise at runtime
VOLUME ["/app/graphdb"]

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
