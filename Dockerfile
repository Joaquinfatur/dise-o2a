# Multi-stage build
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Production stage - CAMBIO AQUÍ
FROM eclipse-temurin:17-jre-alpine  # ← JRE en vez de JDK

RUN apk add --no-cache curl

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar  # ← Wildcard para cualquier JAR

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]