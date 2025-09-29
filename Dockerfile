# Etapa 1: Build con Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copiamos los archivos del proyecto
COPY pom.xml .
COPY src ./src

# Compilamos el proyecto (esto genera un .jar)
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final más liviana
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copiamos el .jar compilado desde la imagen anterior
COPY --from=builder /app/target/*.jar app.jar

# Puerto en el que corre la API (ajustalo si usás otro)
EXPOSE 8080

# Comando para correr la API
CMD ["java", "-jar", "app.jar"]
