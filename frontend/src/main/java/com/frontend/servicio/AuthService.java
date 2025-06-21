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
import com.frontend.SessionManager;
import com.shared.modelos.LoginDTO;
import com.shared.modelos.RegisterDTO;

public class AuthService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String backendBaseUrl = "http://localhost:8080/api/v1/auth";

    public AuthService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        // módulo para manejar LocalDate
        this.objectMapper.registerModule(new JavaTimeModule());

    }

    public CompletableFuture<Boolean> login(String correo, String contrasena) {
        LoginDTO loginRequest = new LoginDTO(correo, contrasena);

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
                            String token = response.body();
                            // guardar el token en el SessionManager
                            SessionManager.getInstance().setAuthToken(token);
                            System.out.println("Login exitoso. Token recibido: " + token);

                            System.out.println("Token guardado en el SessionManager: "
                                    + SessionManager.getInstance().getAuthToken());
                            return true;
                        } else {
                            // Login fallido, mostramos el error
                            System.err.println("Error en el login. Status Code: " + response.statusCode());
                            System.err.println("Body: " + response.body());

                            // invalidar el token en caso de error
                            SessionManager.getInstance().setAuthToken(null);
                            return false;
                        }
                    })
                    // Maneja cualquier excepcion que ocurra durante la comunicaion con el backend.
                    .exceptionally(e -> {
                        System.err.println("Error al comunicarse con el backend para el login: " + e.getMessage());
                        e.printStackTrace();
                        // invalidar el token en caso de error
                        SessionManager.getInstance().setAuthToken(null);
                        return false;
                    });
        } catch (IOException e) {
            System.err.println("Error al crear el cuerpo de la petición de login: " + e.getMessage());
            e.printStackTrace();
            SessionManager.getInstance().setAuthToken(null);
            return CompletableFuture.completedFuture(false);
        }
    }

    public CompletableFuture<HttpResponse<String>> enviarCredencialesRegistro(String nombre, String contraseña,
            String correo, String apellido,
            LocalDate fechaNac) {
        try {
            RegisterDTO registerDTO = RegisterDTO.builder().nombre(nombre).contrasena(contraseña).correo(correo)
                    .apellido(apellido).fechaNac(fechaNac).build();

            // Convertir el DTO a JSON
            String requestBody = objectMapper.writeValueAsString(registerDTO);

            // Crear la petición POST al endpoint de registro del backend
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(backendBaseUrl + "/register")) // Endpoint para el registro
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // Método para enviar solicitudes al backend que requieran autenticacion
    public CompletableFuture<HttpResponse<String>> enviarSolicitudAutenticada(String endpoint, String method) {
        if (SessionManager.getInstance().getAuthToken() == null) {
            System.err.println("No hay token de autenticación disponible.");
            return CompletableFuture
                    .failedFuture(new IllegalStateException("No hay token de autenticación disponible."));
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(backendBaseUrl + endpoint))
                .header("Authorization", "Bearer " + SessionManager.getInstance().getAuthToken());

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
