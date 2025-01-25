# Stage 1: Build the sbt project
FROM openjdk:17-slim AS builder

# Install sbt and dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    gnupg && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" > /etc/apt/sources.list.d/sbt.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x99e82a75642ac823" | apt-key add - && \
    apt-get update && apt-get install -y sbt && apt-get clean

# Set the working directory
WORKDIR /build

# Copy the project files
COPY build.sbt .
COPY project /build/project
RUN sbt update

# Copy the remaining source code
COPY . .

# Build the fat JAR
RUN sbt clean assembly

# Stage 2: Create runtime image
FROM openjdk:17-slim

# Set the working directory
WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=builder /build/target/scala-3.6.2/*.jar /app/app.jar

# Copy the wait-for-it script into the image
COPY wait-for-it.sh /app/wait-for-it.sh

# Expose the port your application listens on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "/app/app.jar"]
