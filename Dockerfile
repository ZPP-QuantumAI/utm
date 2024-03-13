# Use a base image with JDK 17 installed
FROM openjdk:17-slim as build

# Install sbt
ARG SBT_VERSION=1.6.2
RUN apt-get update -qq && \
    apt-get install -qq -y --no-install-recommends curl && \
    mkdir /working/ && \
    cd /working/ && \
    curl -L -o sbt-$SBT_VERSION.deb https://repo.scala-sbt.org/scalasbt/debian/sbt-$SBT_VERSION.deb && \
    dpkg -i sbt-$SBT_VERSION.deb && \
    rm sbt-$SBT_VERSION.deb && \
    apt-get update && \
    apt-get install sbt && \
    cd && \
    rm -r /working/ && \
    sbt sbtVersion

# Set the working directory in the Docker image
WORKDIR /app

# Copy the project files to the Docker image
COPY . .

# Compile the project
RUN sbt clean compile

# Stage 2 - Building the image for execution
FROM openjdk:17-slim

WORKDIR /app

# Copy the build artifacts from the previous stage
COPY --from=build /app/target/scala-2.13/*.jar ./app.jar

# Define the command to run the application
CMD ["java", "-jar", "app.jar"]
