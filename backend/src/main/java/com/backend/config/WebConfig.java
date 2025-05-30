package com.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * mapea las rutas de las imagenes de cada receta a los archivos fisicos
 * del sistema de archivos del servidor (uploads/recetas-imagenes)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.upload.dir:${user.home}}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapea /images/** a la ubicación física de los archivos
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadDir + "/recetas-imagenes/");

        System.out.println("Directorio de imágenes mapeado: " + uploadDir + "/recetas-imagenes/");
    }
}
