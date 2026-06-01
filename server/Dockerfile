FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

# Set the working directory in the container
WORKDIR /usr/src/app

# Copy the pom.xml and source code to the container
COPY ./pom.xml ./pom.xml
COPY ./src ./src

# Build the application using Maven, skipping tests and caching dependencies
RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests

# Use a smaller base image for the runtime environment
FROM eclipse-temurin:21-jre-alpine

WORKDIR /usr/src/app

# Copy the built JAR file from the builder stage to the runtime stage
COPY --from=BUILDER /usr/src/app/target/*.jar api.jar

# Run the application through bash shell
ENTRYPOINT [ "sh", "-c", "java -jar api.jar" ]