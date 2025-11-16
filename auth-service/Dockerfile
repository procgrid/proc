FROM openjdk:21-jre-slim

LABEL maintainer="ProcGrid Team <support@procgrid.com>"
LABEL service="auth-service"

# Create application user
RUN addgroup --system app && adduser --system app --ingroup app

# Set working directory
WORKDIR /app

# Copy JAR file
COPY target/auth-service-*.jar app.jar

# Change ownership
RUN chown -R app:app /app

# Switch to non-root user
USER app

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8081/auth/health || exit 1

# Expose port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=docker"]