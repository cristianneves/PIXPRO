# syntax=docker/dockerfile:1.7

# -------- Build stage --------
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

# Copy parent POM and module POM first to leverage build cache
# IMPORTANT: Build from the pixpro-backend folder as context
#   docker build -f project-service/Dockerfile .
COPY pom.xml ./
COPY project-service/pom.xml ./project-service/pom.xml

# Copy sources for the module
COPY project-service/src ./project-service/src

# Build only the project-service module using its POM
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -f project-service/pom.xml -DskipTests package

# -------- Runtime stage --------
FROM eclipse-temurin:17-jre-alpine AS runtime

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Environment knobs (Spring Boot will read SERVER_PORT automatically)
ENV SPRING_PROFILES_ACTIVE=default \
    JAVA_OPTS="" \
    SERVER_PORT=8082

# Copy the fat jar from the builder stage
COPY --from=builder /workspace/project-service/target/*.jar /app/app.jar

# Expose the default port
EXPOSE 8082

# Healthcheck (optional) – adjust endpoint if you have actuator
# HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
#   CMD wget -qO- http://localhost:${SERVER_PORT}/actuator/health | grep -q 'UP' || exit 1

USER appuser

# Use exec form; allow extra JVM flags via JAVA_OPTS
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
