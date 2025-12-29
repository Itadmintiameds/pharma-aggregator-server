## Multi-stage Docker build for Spring Boot application

FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/pharma-aggregator-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Default profile can be overridden at runtime: -e SPRING_PROFILES_ACTIVE=test
ENV SPRING_PROFILES_ACTIVE=dev

ENTRYPOINT ["java", "-jar", "/app/app.jar"]


