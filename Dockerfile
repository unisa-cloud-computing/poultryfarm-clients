# ── Stage 1: Build ──────────────────────────────────────────────────
FROM maven:3.9-amazoncorretto-17 AS build

WORKDIR /app

# Cache delle dipendenze Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia sorgenti e build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Stage 2: Runtime ────────────────────────────────────────────────
FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
