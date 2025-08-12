# Use Java 17 base image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven wrapper and project files
COPY . .

# Build the JAR
RUN ./mvnw clean package -DskipTests

# Run the JAR
CMD ["java", "-jar", "target/UserPI-0.0.1-SNAPSHOT.jar"]
