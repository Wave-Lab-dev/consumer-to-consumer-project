# Multi-stage build for Spring Boot application
FROM eclipse-temurin:17-jdk as builder

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies (this layer will be cached if build.gradle doesn't change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build --no-daemon -x test

# Production stage
FROM eclipse-temurin:17-jre

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to app user
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Add runtime environment variables from build args
ARG SPRING_DATASOURCE_URL
ARG SPRING_DATASOURCE_USERNAME
ARG SPRING_DATASOURCE_PASSWORD
ARG AWS_S3_BUCKET
ARG AWS_REGION
ARG AWS_ACCESS_KEY
ARG AWS_SECRET_KEY
ARG JWT_SECRET
ARG JWT_TOKEN_VALIDITY_IN_SECONDS
ARG SMTP_DOMAIN
ARG SENDER_EMAIL
ARG SENDER_PASSWORD
ARG SPRING_REDIS_HOST
ARG SPRING_REDIS_PORT

ENV SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}
ENV SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
ENV SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
ENV AWS_S3_BUCKET=${AWS_S3_BUCKET}
ENV AWS_REGION=${AWS_REGION}
ENV AWS_ACCESS_KEY=${AWS_ACCESS_KEY}
ENV AWS_SECRET_KEY=${AWS_SECRET_KEY}
ENV JWT_SECRET=${JWT_SECRET}
ENV JWT_TOKEN_VALIDITY_IN_SECONDS=${JWT_TOKEN_VALIDITY_IN_SECONDS}
ENV SMTP_DOMAIN=${SMTP_DOMAIN}
ENV SENDER_EMAIL=${SENDER_EMAIL}
ENV SENDER_PASSWORD=${SENDER_PASSWORD}
ENV SPRING_REDIS_HOST=${SPRING_REDIS_HOST}
ENV SPRING_REDIS_PORT=${SPRING_REDIS_PORT}

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
