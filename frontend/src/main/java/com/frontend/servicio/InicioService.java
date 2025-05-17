package com.frontend.servicio;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.frontend.SessionManager;
import com.shared.modelos.Receta;

public class InicioService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String backendBaseUrl = "http://localhost:8080/api/v1";

    public InicioService() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper.registerModule(new JavaTimeModule()); // Registrar el módulo para manejar LocalTime
    }

    /*
     * Hace una peticion al backend para guardar una receta
     * envia los datos de la receta + el token del usuario
     */
    public CompletableFuture<HttpResponse<String>> enviarRecetaAlBackend(Receta receta) {
        try {
            String requestBody = objectMapper.writeValueAsString(receta);
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(backendBaseUrl + "/receta"))
                    .header("Content-Type", "application/json");

            if (SessionManager.getInstance().getAuthToken() != null) {
                requestBuilder.header("Authorization", "Bearer " + SessionManager.getInstance().getAuthToken());
            }

            HttpRequest request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

            return httpClient
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.err.println("Error al crear la petición para enviar la receta: " + e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /*
     * Hace una peticion al backend para obtener
     * todas las recetas de un usuario por su ID (se envía en el token)
     */
    public List<Receta> obtenerRecetasDelUsuario() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendBaseUrl + "/recetas"))
                .header("Authorization", "Bearer " + SessionManager.getInstance().getAuthToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            // Si la respuesta es exitosa, deserializar el cuerpo y devolver la lista de
            // recetas
            return objectMapper.readValue(response.body(), new TypeReference<List<Receta>>() {
            });
        } else {
            // Si ocurre un error, lanzar una excepción con el mensaje del cuerpo
            throw new RuntimeException("Error al obtener recetas: " + response.body());
        }
    }

}
