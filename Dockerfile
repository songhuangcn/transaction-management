FROM maven:3.9-eclipse-temurin-21 AS base
WORKDIR /app
# Copy pom.xml first to leverage Docker layer caching
COPY pom.xml .
# Download dependencies (this layer will be cached unless pom.xml changes)
RUN mvn dependency:go-offline

FROM base AS builder
# Copy source code
COPY src ./src
# Build application
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy jar file from build stage
COPY --from=builder /app/target/*.jar app.jar
# Create non-root user and change file ownership
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup && \
    chown -R appuser:appgroup /app
# Switch to non-root user
USER appuser
EXPOSE 8080
CMD ["java", "-jar", "app.jar"] 
