# Utiliza una imagen base de OpenJDK con Java 17. 'jdk-slim' es más pequeño.
FROM openjdk:21-jdk-slim

# Define el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el JAR ejecutable de tu aplicación Spring Boot al contenedor
# Asegúrate de que el nombre del JAR (backend-0.0.1-SNAPSHOT.jar) coincida exactamente
# con el nombre del archivo que se generó en el paso 1.
COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

# Expone el puerto en el que tu aplicación Spring Boot escucha (por defecto 8080)
EXPOSE 8080

# Define el comando que se ejecutará cuando se inicie el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]