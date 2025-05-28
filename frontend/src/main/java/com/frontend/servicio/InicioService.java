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

            // Generar un límite único para usarlo en el multipart
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();

            // Define la secuencia de caracteres de "retorno de carro y salto de línea"
            String CRLF = "\r\n";
            // Construir el cuerpo multipart
            StringBuilder requestBody = new StringBuilder();

            /*
             * Esta sección construye la primera "parte" del
             * multipart form-data, que
             * contiene los datos de la receta en formato JSON.
             */
            requestBody.append("--").append(boundary).append(CRLF);
            requestBody.append("Content-Disposition: form-data; name=\"receta\"").append(CRLF);
            requestBody.append("Content-Type: application/json").append(CRLF).append(CRLF);
            requestBody.append(objectMapper.writeValueAsString(receta)).append(CRLF);

            // Si hay una imagen, añadirla al multipart
            byte[] imageBytes = null;
            if (imagenFile != null) {
                imageBytes = Files.readAllBytes(imagenFile.toPath());
                requestBody.append("--").append(boundary).append(CRLF);
                requestBody.append("Content-Disposition: form-data; name=\"imagen\"; filename=\"")
                        .append(imagenFile.getName()).append("\"").append(CRLF);
                requestBody.append("Content-Type: ").append(Files.probeContentType(imagenFile.toPath()))
                        .append(CRLF).append(CRLF);
            }

            // Construir la petición HTTP
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(backendBaseUrl + "/receta"))
                    // establece el encaabezado y el tipo de cuerpo
                    // especifica el boundary que se esta usando
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary);

            if (SessionManager.getInstance().getAuthToken() != null) {
                requestBuilder.header("Authorization", "Bearer " + SessionManager.getInstance().getAuthToken());
            }

            // Combinar el cuerpo de la petición
            // Esta sección es donde se ensambla el cuerpo final de la petición HTTP.
            byte[] bodyBytes;
            if (imageBytes != null) {
                // Combinar el texto del formulario, la imagen y el límite final
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
                outputStream.write(imageBytes);
                outputStream.write((CRLF + "--" + boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8));
                bodyBytes = outputStream.toByteArray();
            } else {
                // Solo el texto del formulario y el límite final
                bodyBytes = (requestBody.toString() + "--" + boundary + "--" + CRLF)
                        .getBytes(StandardCharsets.UTF_8);
            }

            // Crear y enviar la petición
            HttpRequest request = requestBuilder
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

    public CompletableFuture<HttpResponse<String>> actualizarReceta(Receta receta) {
        try {
            String requestBody = objectMapper.writeValueAsString(receta);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(backendBaseUrl + "/receta/" + receta.getId()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + SessionManager.getInstance().getAuthToken())
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

}
