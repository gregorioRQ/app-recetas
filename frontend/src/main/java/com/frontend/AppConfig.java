package com.frontend;

//carga la variable de entorno
public class AppConfig {
    private static final String DEFAULT_BACKEND_URL = "http://localhost:8080";

    public static String getBackendBaseUrl() {
        String backendUrl = System.getenv("BACKEND_BASE_URL");

        // Asegurarse de que la URL no termine en /
        if (backendUrl != null && backendUrl.endsWith("/")) {
            backendUrl = backendUrl.substring(0, backendUrl.length() - 1);
        }
        return backendUrl != null ? backendUrl : DEFAULT_BACKEND_URL;
    }
}
