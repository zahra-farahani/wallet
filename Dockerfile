# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
COPY --from=builder /app/target/*.jar app.jar
ENV JAVA_OPTS="-Xmx512m"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app.jar"]