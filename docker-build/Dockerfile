# Stage 1: Build the application
# Use a specific Maven version with a compatible JDK for reproducibility
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

# Set the working directory
WORKDIR /app 

# Copy pom.xml first to leverage Docker cache for dependencies
COPY pom.xml ./
# Download dependencies. -B runs in batch mode (non-interactive)
RUN mvn dependency:go-offline -B

# Copy the rest of the application source code
COPY src ./src

# Package the application.
# -DskipTests is common for CI/CD to speed up builds.
# The final JAR name should be configured in pom.xml to be 'bug-tracker.jar'
RUN mvn clean package -DskipTests

# #############################################################################

# Stage 2: Create the runtime image
# Use a slim JRE image for a smaller final footprint
FROM openjdk:17-slim

WORKDIR /app

# Copy the built JAR from the builder stage.
# This assumes your pom.xml has <finalName>bug-tracker</finalName> in the <build> section.
COPY --from=builder /app/target/bug-tracker.jar .

# Command to run the application
# Explicitly set the active profile to 'dev' to ensure the in-memory database
# and data loader are used, making the container's behavior self-contained.
CMD ["java", "-Dspring.profiles.active=dev", "-jar", "bug-tracker.jar"]