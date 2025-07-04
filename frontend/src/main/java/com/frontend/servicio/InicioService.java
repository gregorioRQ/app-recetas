package com.frontend.servicio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
     * este metodo se encarga de empaquetar datos estructurados
     * (receta JSON) y un archivo binario (imagen) en un formato
     * compatible con multipart/form-data y enviarlos
     * a un backend de manera asíncrona
     */
    public CompletableFuture<HttpResponse<String>> enviarRecetaAlBackend(Receta receta, File imagenFile) {
        try {
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            byte[] bodyBytes = construirMultipartBody(receta, imagenFile, boundary);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(backendBaseUrl + "/receta"))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("Authorization", "Bearer " + SessionManager.getInstance().getAuthToken())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

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

    public CompletableFuture<HttpResponse<String>> eliminarRecetaPorId(Long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendBaseUrl + "/receta/" + id))
                .header("Authorization", "Bearer " + SessionManager.getInstance().getAuthToken())
                .header("Content-Type", "application/json")
                .DELETE()
                .build();
        return HttpClient.newHttpClient().sendAsync(request, HttpResponse.
        // convierte el cuerpo de la respuesta "body" en un objeto de tipo String
                BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> actualizarReceta(Receta receta, File imagenFile) {
        try {
            // si no hay imagen hacer una peticion put normal
            /*
             * if (imagenFile == null) {
             * String requestBody = objectMapper.writeValueAsString(receta);
             * HttpRequest request = HttpRequest.newBuilder()
             * .uri(URI.create(backendBaseUrl + "/receta/" + receta.getId()))
             * .header("Content-Type", "application/json")
             * .header("Authorization", "Bearer " +
             * SessionManager.getInstance().getAuthToken())
             * .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
             * .build();
             * 
             * return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
             * }
             */
            // si hay una nueva imagen, usar multipart
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            byte[] bodyBytes = construirMultipartBody(receta, imagenFile, boundary);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(backendBaseUrl + "/receta/" + receta.getId()))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("Authorization", "Bearer " + SessionManager.getInstance().getAuthToken())
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<HttpResponse<String>> cerrarSesion() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendBaseUrl + "/auth/logout"))
                .header("Authorization", "Bearer " + SessionManager.getInstance().getAuthToken())
                .POST(HttpRequest.BodyPublishers.noBody()) // Usar POST sin cuerpo
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    // metodo aux
    private byte[] construirMultipartBody(Receta receta, File imagenFile, String boundary) throws IOException {
        String CRLF = "\r\n";
        StringBuilder requestBody = new StringBuilder();

        // Parte de la receta
        requestBody.append("--").append(boundary).append(CRLF);
        requestBody.append("Content-Disposition: form-data; name=\"receta\"").append(CRLF);
        requestBody.append("Content-Type: application/json").append(CRLF).append(CRLF);
        requestBody.append(objectMapper.writeValueAsString(receta)).append(CRLF);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));

        // Parte de la imagen (si existe)
        if (imagenFile != null) {
            byte[] imageBytes = Files.readAllBytes(imagenFile.toPath());
            String imageHeader = "--" + boundary + CRLF +
                    "Content-Disposition: form-data; name=\"imagen\"; filename=\"" +
                    imagenFile.getName() + "\"" + CRLF +
                    "Content-Type: " + Files.probeContentType(imagenFile.toPath()) +
                    CRLF + CRLF;

            outputStream.write(imageHeader.getBytes(StandardCharsets.UTF_8));
            outputStream.write(imageBytes);
            outputStream.write(CRLF.getBytes(StandardCharsets.UTF_8));
        }

        // Límite final
        outputStream.write(("--" + boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8));

        return outputStream.toByteArray();
    };

}
