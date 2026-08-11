# Multi-stage Dockerfile for Spring Boot Backend

# Stage 1: Build stage with Maven and Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy static frontend resources (if built) and Java source code
COPY src ./src
COPY frontend ./frontend

# Build executable JAR skipping tests (tests ran in CI)
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage with lightweight Java 21 JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install Tesseract OCR package for Alpine runtime
RUN apk add --no-cache tesseract-ocr tesseract-ocr-data-eng

# Create storage directory for claims
RUN mkdir -p storage/claims

# Copy packaged JAR from builder stage
COPY --from=builder /app/target/tpa-claim-processor-1.0.0-SNAPSHOT.jar app.jar

# Expose backend port 7002
EXPOSE 7002

# Set default environment variables
ENV SERVER_PORT=7002 \
    DB_HOST=postgres \
    DB_PORT=5432 \
    DB_NAME=tpa_claims_db \
    DB_USERNAME=postgres \
    DB_PASSWORD=postgres \
    UPLOAD_DIR=storage/claims

# Run Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
