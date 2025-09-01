FROM maven:3.9.4-eclipse-temurin-17 AS build
COPY . . 
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-alpine
COPY --from=build /target/my-app-name-1.0-SNAPSHOT*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]