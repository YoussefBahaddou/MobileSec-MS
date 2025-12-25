# Build Stage
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run Stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Standard Spring Boot Port, adjust if needed
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
