# Build stage
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy project source code
COPY . .

# Runtime stage
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/transaction-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]