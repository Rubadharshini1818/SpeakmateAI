FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY backend/.mvn .mvn
COPY backend/mvnw mvnw
COPY backend/pom.xml pom.xml
COPY backend/src src
COPY Frontend Frontend

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/Frontend /app/Frontend

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
