# Сборка сервера Заң KZ в Docker-образ (две стадии: сборка и запуск)

# 1) Сборка: Maven + Java 21 собирают jar-файл
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -q -DskipTests package

# 2) Запуск: только Java 21 (JRE) и готовый jar — образ получается меньше
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]