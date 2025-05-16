package com.frontend.servicio;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontend.SessionManager;
import com.shared.modelos.Receta;

public class InicioService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String backendBaseUrl = "http://localhost:8080/api/v1";

    public InicioService() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

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

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.err.println("Error al crear la petición para enviar la receta: " + e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
