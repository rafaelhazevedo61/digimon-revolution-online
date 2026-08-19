FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn

RUN chmod +x mvnw
RUN ./mvnw -B -DskipTests dependency:go-offline

COPY src src

RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:17-jre

RUN useradd --system --create-home --uid 10001 dro

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

USER dro

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
