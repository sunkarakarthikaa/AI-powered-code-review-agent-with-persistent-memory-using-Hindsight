# --- Stage 1: build ---
# Uses a full JDK + Maven image, but this image is never shipped — it only
# produces the jar. Keeping build tools out of the final image is the whole
# reason for a multi-stage build: smaller image, smaller attack surface.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first and download dependencies before copying source code.
# Docker caches each layer — as long as pom.xml doesn't change, this
# dependency-download layer is reused on every rebuild instead of
# re-downloading every dependency every time you change one Java file.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Stage 2: run ---
# Slim JRE-only image (no compiler, no Maven) — this is what actually ships.
# Matches pom.xml's <java.version>17</java.version>.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Run as a non-root user — a container running as root is a well-known
# escalation risk if the app is ever compromised. Worth mentioning
# unprompted in an interview; it's a small line that signals real
# security awareness, not just "it works."
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/target/pr-guardian-lite-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]