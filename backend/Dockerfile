# =============================================
# Stage 1: Build
# =============================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Копируем pom.xml и скачиваем зависимости (кэшируется)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходники и собираем
COPY src ./src
RUN mvn clean package -DskipTests

# =============================================
# Stage 2: Runtime
# =============================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Создаём пользователя для безопасности
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Копируем JAR из build stage
COPY --from=build /app/target/*.jar app.jar

# Порт
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/actuator/health || exit 1

# Запуск
ENTRYPOINT ["java", "-jar", "app.jar"]