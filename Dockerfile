FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENV JAVA_OPTS="-Xmx512m"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app.jar"]