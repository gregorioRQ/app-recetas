package com.backend.controlador;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.jwt.JwtTokenProvider;
import com.backend.modelos.RecetaEntity;
import com.backend.servicio.RecetaService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shared.modelos.Receta;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
public class RecetaController {
    @Autowired
    private RecetaService recetaService;
    @Autowired
    private JwtTokenProvider provider;

    /*
     * @RequestPart para el manejo de las partes de la solicitud
     * multipart/form-data.
     */
    @PostMapping(value = "/receta", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> crearReceta(
            @RequestPart("receta") String recetaJson,
            @RequestPart(value = "imagen", required = true) MultipartFile imagen,
            HttpServletRequest request) {

        // Validar autenticación
        String jwt = getJwtFromRequest(request);
        if (!provider.validateToken(jwt)) {
            return new ResponseEntity<>("Token inválido", HttpStatus.UNAUTHORIZED);
        }

        try {

            // Validar tamaño de la imagen
            if (imagen.getSize() > 5 * 1024 * 1024) { // 5 MB
                return new ResponseEntity<>("El archivo de imagen no debe exceder los 5 MB.", HttpStatus.BAD_REQUEST);
            }

            // valida la resolucion minima de la imagen
            BufferedImage bufferedImage = ImageIO.read(imagen.getInputStream());
            if (bufferedImage == null) {
                return new ResponseEntity<>("El archivo no es una imagen válida.", HttpStatus.BAD_REQUEST);
            }
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            if (width < 500 || height < 500) {
                return new ResponseEntity<>("La imagen debe tener como mínimo una resolución de 500x500.",
                        HttpStatus.BAD_REQUEST);
            }

            // convertir el json a un mapa para validaciones
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            Map<String, Object> recetaMap = mapper.readValue(recetaJson, new TypeReference<Map<String, Object>>() {
            });

            Map<String, String> errores = validarCamposReceta(recetaMap);

            if (!errores.isEmpty()) {
                StringBuilder mensajeErrores = new StringBuilder("Por favor, corrige los siguientes errores:\n");
                errores.forEach((campo, mensaje) -> mensajeErrores.append("- ").append(mensaje).append("\n"));
                return new ResponseEntity<>(mensajeErrores.toString(), HttpStatus.BAD_REQUEST);
            }

            // convertir el mapa a objeto receta
            Receta receta = mapper.convertValue(recetaMap, Receta.class);

            recetaService.guardarReceta(receta, imagen);
            return new ResponseEntity<>("Receta creada con éxito.", HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            return new ResponseEntity<>("Error al crear la receta: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
     * Se usa el objeto HttpServletRequest para acceder al
     * encabezado "Authorization" para obtener el token y despues
     * el "subject".
     */
    @GetMapping("/recetas")
    public ResponseEntity<?> todasLasRecetasUsuarioID(HttpServletRequest request) {

        try {
            String jwt = getJwtFromRequest(request);
            if (!provider.validateToken(jwt)) {
                return new ResponseEntity<>("Token inválido", HttpStatus.UNAUTHORIZED);
            }
            Long userId = provider.getUserIdFromJWT(jwt);

            List<RecetaEntity> recetas = recetaService.todasLasRecetasPorUsuarioId(userId);
            return new ResponseEntity<>(recetas, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            System.err.println("Error: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Ocurrió un error inesperado. Por favor, inténtalo más tarde.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @DeleteMapping("/receta/{id}")
    public ResponseEntity<?> eliminarRecetaPorId(@PathVariable("id") Long id, HttpServletRequest request) {
        try {
            String jwt = getJwtFromRequest(request);
            if (!provider.validateToken(jwt)) {
                return new ResponseEntity<>("Token inválido", HttpStatus.UNAUTHORIZED);
            }
            recetaService.eliminarRecetaPorId(id);
            return new ResponseEntity<>("Receta eliminada", HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("No se pudo eliminar: " + e.getMessage());
            return new ResponseEntity<>("No se pudo eliminar: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/receta/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editarReceta(
            @PathVariable("id") Long id,
            @RequestPart("receta") String recetaJson,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            HttpServletRequest request) {
        try {
            String jwt = getJwtFromRequest(request);
            if (!provider.validateToken(jwt)) {
                return new ResponseEntity<>("Token inválido", HttpStatus.UNAUTHORIZED);
            }

            // Convertir el JSON a objeto Receta
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            Receta receta = mapper.readValue(recetaJson, Receta.class);

            // Actualizar la receta
            RecetaEntity recetaActualizada = recetaService.actualizarReceta(id, receta, imagen);
            return new ResponseEntity<>(recetaActualizada, HttpStatus.OK);

        } catch (EntityNotFoundException e) {
            System.err.println("Error: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error al actualizar la receta: " + e.getMessage());
            return new ResponseEntity<>("Error al actualizar la receta",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/receta/{id}")
    public ResponseEntity<?> editarRecetaParcial(@PathVariable("id") Long id, @RequestBody Map<String, Object> cambios,
            HttpServletRequest request) {
        try {
            String jwt = getJwtFromRequest(request);
            if (!provider.validateToken(jwt)) {
                return new ResponseEntity<>("Token inválido", HttpStatus.UNAUTHORIZED);
            }

            RecetaEntity recetaActualizada = recetaService.actualizarRecetaParcial(id, cambios);
            return new ResponseEntity<>(recetaActualizada, HttpStatus.OK);
        } catch (EntityNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Método auxiliar para extraer el JWT de la solicitud
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Map<String, String> validarCamposReceta(Map<String, Object> recetaJson) {
        Map<String, String> errores = new HashMap<>();

        // Validar nombre
        String nombre = (String) recetaJson.get("nombre");
        if (nombre == null || nombre.trim().isEmpty()) {
            errores.put("nombre", "El campo 'nombre' es obligatorio.");
        } else if (nombre.length() < 3 || nombre.length() > 110) {
            errores.put("nombre", "El campo 'nombre' debe tener entre 3 y 110 caracteres.");
        } else if (!nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
            errores.put("nombre", "El campo 'nombre' solo puede contener letras y espacios.");
        }

        // Validar ingredientes
        String ingredientes = (String) recetaJson.get("ingredientes");
        if (ingredientes == null || ingredientes.trim().isEmpty()) {
            errores.put("ingredientes", "El campo 'ingredientes' es obligatorio.");
        } else if (ingredientes.length() < 3 || ingredientes.length() > 400) {
            errores.put("ingredientes", "El campo 'ingredientes' debe tener entre 3 y 400 caracteres.");
        }

        // Validar instrucciones
        String instrucciones = (String) recetaJson.get("instrucciones");
        if (instrucciones == null || instrucciones.trim().isEmpty()) {
            errores.put("instrucciones", "El campo 'instrucciones' es obligatorio.");
        } else if (instrucciones.length() < 3 || instrucciones.length() > 4000) {
            errores.put("instrucciones", "El campo 'instrucciones' debe tener entre 3 y 4000 caracteres.");
        }

        // Validar porciones
        Object porcionesObj = recetaJson.get("porciones");
        if (porcionesObj == null) {
            errores.put("porciones", "El campo 'porciones' es obligatorio.");
        } else {
            try {
                int porciones = Integer.parseInt(porcionesObj.toString());
                if (porciones <= 0) {
                    errores.put("porciones", "El campo 'porciones' debe ser un número positivo.");
                }
                if (porcionesObj.toString().trim().length() > 4) {
                    errores.put("porciones", "El campo 'porciones' no puede exceder numeros de 4 cifras");
                }
            } catch (NumberFormatException e) {
                errores.put("porciones", "El campo 'porciones' debe ser un número válido.");
            }
        }

        return errores;
    }

}
