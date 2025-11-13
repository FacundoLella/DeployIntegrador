FROM eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=target/mycar-0.0.1-SNAPSHOT.jar 
COPY ${JAR_FILE} mycar-0.0.1-SNAPSHOT.jar 
EXPOSE 8080
ENTRYPOINT ["java","-jar","mycar-0.0.1-SNAPSHOT.jar"]