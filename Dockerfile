# Start with a base image containing Java runtime
FROM eclipse-temurin:17-jdk-alpine as build

# Set the working directory
WORKDIR /app

# Copy the Maven wrapper and pom.xml file
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make the Maven wrapper executable
RUN chmod +x ./mvnw

# Download all required dependencies
# This step is separated to leverage Docker's cache
# If dependencies don't change, this layer won't rebuild
RUN ./mvnw dependency:go-offline -B

# Copy the project source
COPY src src

# Package the application
RUN ./mvnw package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Stage 2: Create the final image
FROM eclipse-temurin:17-jre-alpine

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# Create a volume for temporary files
VOLUME /tmp

# Add a non-root user for improved security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built artifact from the build stage
ARG DEPENDENCY=/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Set the entry point
ENTRYPOINT ["java","-cp","app:app/lib/*","org.doctech.DocTechApplication"]

# Expose the application port
EXPOSE 8080