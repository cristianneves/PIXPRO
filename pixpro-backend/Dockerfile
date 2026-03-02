# syntax=docker/dockerfile:1.7

# ============================================
# Multi-Service Dockerfile for PixPro Backend
# ============================================
# This Dockerfile builds Java-based microservices by using the SERVICE_NAME build arg.
# Note: processing-service (Python) has its own Dockerfile under processing-service/.
#
# Usage:
#   docker build --build-arg SERVICE_NAME=auth-service -t pixpro/auth-service .
#   docker build --build-arg SERVICE_NAME=project-service -t pixpro/project-service .
#   docker build --build-arg SERVICE_NAME=discovery-server -t pixpro/discovery-server .
#   docker build --build-arg SERVICE_NAME=notification-service -t pixpro/notification-service .
#   docker build --build-arg SERVICE_NAME=api-gateway -t pixpro/api-gateway .
#   (Python) processing-service is built with processing-service/Dockerfile

# -------- Build stage --------
FROM maven:3.9.9-eclipse-temurin-17 AS builder

# Accept service name as build argument
ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

WORKDIR /workspace

# Validate SERVICE_NAME
RUN if [ -z "$SERVICE_NAME" ]; then \
        echo "ERROR: SERVICE_NAME build argument is required" && \
        echo "Usage: docker build --build-arg SERVICE_NAME=<service> -t <tag> ." && \
        echo "Valid services: auth-service, project-service, discovery-server, notification-service, api-gateway" && \
        exit 1; \
    fi

# Copy parent POM + module POM + sources
COPY pom.xml ./pom.xml
COPY ${SERVICE_NAME}/pom.xml ${SERVICE_NAME}/pom.xml
COPY ${SERVICE_NAME}/src ${SERVICE_NAME}/src

# Build the specific service module with its own pom (no aggregator scanning)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -f ${SERVICE_NAME}/pom.xml -DskipTests package

# -------- Runtime stage --------
FROM eclipse-temurin:17-jre-alpine AS runtime

# Accept service name as build argument
ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Default environment variables (can be overridden at runtime)
ENV SPRING_PROFILES_ACTIVE=default \
    JAVA_OPTS=""

# Copy the fat jar from the builder stage
COPY --from=builder /workspace/${SERVICE_NAME}/target/*.jar /app/app.jar

# Service will determine its own port from application.properties/yml
# Common ports: 8761 (discovery), 8080 (gateway), 8081 (auth), 8082 (project), 8083 (notification), 5000 (processing)
EXPOSE 8080 8081 8082 8083 8761 5000

USER appuser

# Use exec form; allow extra JVM flags via JAVA_OPTS
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
