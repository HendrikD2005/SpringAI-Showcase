FROM eclipse-temurin:21-jdk-alpine

WORKDIR .

COPY target/SpringAI-Showcase-1.0.0-SNAPSHOT.jar .

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "SpringAI-Showcase-1.0.0-SNAPSHOT.jar"]