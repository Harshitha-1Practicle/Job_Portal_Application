FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q -P skip-frontend
COPY src ./src
RUN ./mvnw package -DskipTests -q -P skip-frontend

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/job-portal-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
