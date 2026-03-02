# syntax=docker/dockerfile:1.7

# -------- Build stage --------
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

# Copy parent POM and module POM first to leverage build cache
# IMPORTANT: Build from the pixpro-backend folder as context
#   docker build -f discovery-server/Dockerfile .
COPY pom.xml ./
COPY discovery-server/pom.xml ./discovery-server/pom.xml

# Copy sources for the module
COPY discovery-server/src ./discovery-server/src

# Build only the discovery-server module using its POM
RUN --mount=type=cache,target=/root/.m2 \
  mvn -B -f discovery-server/pom.xml -DskipTests package

# -------- Runtime stage --------
FROM eclipse-temurin:17-jre-alpine AS runtime

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Environment knobs (Spring Boot will read SERVER_PORT automatically)
ENV SPRING_PROFILES_ACTIVE=default \
    JAVA_OPTS="" \
    SERVER_PORT=8761

# Copy the fat jar from the builder stage
COPY --from=builder /workspace/discovery-server/target/*.jar /app/app.jar

# Expose the default Eureka port
EXPOSE 8761

# Healthcheck (Eureka dashboard is available at /eureka/web)
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:${SERVER_PORT}/actuator/health || exit 1

USER appuser

# Use exec form; allow extra JVM flags via JAVA_OPTS
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
