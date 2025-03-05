# Build stage
FROM eclipse-temurin:21-jdk-jammy AS build
# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/income-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8081

# Set the entrypoint to run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]