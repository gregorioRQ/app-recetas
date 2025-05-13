package com.frontend.servicio;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shared.modelos.RegisterDTO;

public class AuthService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String backendBaseUrl = "http://localhost:8080/api/v1/auth"; //

    public AuthService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        // módulo para manejar LocalDate
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void enviarCredencialesRegistro(String nombre, String contraseña, String correo, String apellido,
            LocalDate fechaNac) {
        try {
            // Crear una instancia de RegisterDTO
            RegisterDTO registerDTO = new RegisterDTO(nombre, apellido, correo, fechaNac, contraseña);

            // Convertir el DTO a JSON
            String requestBody = objectMapper.writeValueAsString(registerDTO);

            // Crear la petición POST al endpoint de registro del backend
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(backendBaseUrl + "/registro")) // Endpoint para el registro
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Enviar la petición y obtener la respuesta
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Mostrar la respuesta del backend en la consola del frontend
            System.out.println("Respuesta del backend (Registro):");
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Body: " + response.body());

        } catch (IOException | InterruptedException e) {
            System.err.println("Error al enviar los datos de registro al backend: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
