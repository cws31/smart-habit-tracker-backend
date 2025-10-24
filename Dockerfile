
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/smart-habit-tracker2.0-0.0.1-SNAPSHOT.jar app.jar

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Expose the port your application runs on (Render will set PORT env variable)
EXPOSE ${PORT:-9090}

# Run the application with environment variables
ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar"]