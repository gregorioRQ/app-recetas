package com.frontend.servicio;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shared.modelos.RegisterDTO;

public class AuthService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String backendBaseUrl = "http://localhost:8080/api/v1/auth";
    private String authToken; // Para almacenar el token

    public AuthService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        // módulo para manejar LocalDate
        this.objectMapper.registerModule(new JavaTimeModule());
        this.authToken = null; // Inicialmente no hay token
    }

    public String getAuthToken() {
        return authToken;
    }

    // SE DISPARA AL LOGUEARSE UN USUARIO
    public CompletableFuture<Boolean> login(String correo, String contrasena) {
        RegisterDTO loginRequest = RegisterDTO.builder().correo(correo).contrasena(contrasena).build();

        try {
            String requestBody = objectMapper.writeValueAsString(loginRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(backendBaseUrl + "/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // sendAsyc() para realizar la peticion de forma asíncrona y evitar
            // bloquear el hilo de la ui del usuario.
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    // Despues de recibir la respuesta éste bloque se ejecuta
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            // Login exitoso, extraemos el token del cuerpo de la respuesta
                            this.authToken = response.body();
                            System.out.println("Login exitoso. Token recibido: " + this.authToken);
                            return true;
                        } else {
                            // Login fallido, mostramos el error
                            System.err.println("Error en el login. Status Code: " + response.statusCode());
                            System.err.println("Body: " + response.body());
                            this.authToken = null;
                            return false;
                        }
                    })
                    // Maneja cualquier excepcion que ocurra durante la comunicaion con el backend.
                    .exceptionally(e -> {
                        System.err.println("Error al comunicarse con el backend para el login: " + e.getMessage());
                        e.printStackTrace();
                        this.authToken = null;
                        return false;
                    });
        } catch (IOException e) {
            System.err.println("Error al crear el cuerpo de la petición de login: " + e.getMessage());
            e.printStackTrace();
            this.authToken = null;
            return CompletableFuture.completedFuture(false);
        }
    }

    // SE DISPARA AL REGISTRAR UN USUARIO
    public void enviarCredencialesRegistro(String nombre, String contraseña, String correo, String apellido,
            LocalDate fechaNac) {
        try {
            // Crear una instancia de RegisterDTO
            RegisterDTO registerDTO = RegisterDTO.builder().nombre(nombre).contrasena(contraseña).correo(correo)
                    .apellido(apellido).fechaNac(fechaNac).build();

            // Convertir el DTO a JSON
            String requestBody = objectMapper.writeValueAsString(registerDTO);

            // Crear la petición POST al endpoint de registro del backend
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(backendBaseUrl + "/registro")) // Endpoint para el registro
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        System.out.println("Respuesta del backend (Registro):");
                        System.out.println("Status Code: " + response.statusCode());
                        System.out.println("Body: " + response.body());
                    })
                    .exceptionally(e -> {
                        System.err.println("Error al enviar los datos de registro al backend: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    });

        } catch (IOException e) {
            System.err.println("Error al enviar los datos de registro al backend: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para enviar solicitudes al backend que requieran autenticacion
    public CompletableFuture<HttpResponse<String>> enviarSolicitudAutenticada(String endpoint, String method) {
        if (authToken == null) {
            System.err.println("No hay token de autenticación disponible.");
            return CompletableFuture
                    .failedFuture(new IllegalStateException("No hay token de autenticación disponible."));
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(backendBaseUrl + endpoint))
                .header("Authorization", "Bearer " + authToken);

        if ("POST".equalsIgnoreCase(method)) {
            requestBuilder.POST(HttpRequest.BodyPublishers.noBody()); // Ejemplo de POST sin cuerpo
        } else if ("PUT".equalsIgnoreCase(method)) {
            requestBuilder.PUT(HttpRequest.BodyPublishers.noBody()); // Ejemplo de PUT sin cuerpo
        } else if ("DELETE".equalsIgnoreCase(method)) {
            requestBuilder.DELETE();
        } else {
            requestBuilder.GET(); // Por defecto es GET
        }

        HttpRequest request = requestBuilder.build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}
