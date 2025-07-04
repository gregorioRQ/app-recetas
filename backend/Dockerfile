# Utiliza una imagen base de OpenJDK con Java 17. 'jdk-slim' es más pequeño.
FROM openjdk:21-jdk-slim

# Define el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el JAR ejecutable de la app Spring Boot al contenedor
COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

# Expone el puerto en el que la app Spring Boot escucha (por defecto 8080)
EXPOSE 8080

# Define el comando que se ejecutará cuando se inicie el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]