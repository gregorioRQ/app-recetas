package com.backend.controlador;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.backend.modelos.RecetaEntity;
import com.backend.servicio.RecetaService;
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

    /*
     * @RequestPart para el manejo de las partes de la solicitud
     * multipart/form-data.
     */
    @PostMapping(value = "/receta", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> crearReceta(
            @RequestPart("receta") String recetaJson,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            HttpServletRequest request) {
        try {

            // Validar autenticación
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new ResponseEntity<>("No autorizado", HttpStatus.UNAUTHORIZED);
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            Receta receta = mapper.readValue(recetaJson, Receta.class);

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
            // Obtener el token del encabezado Authorization
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            String token = authHeader.substring(7); // Remover "Bearer "

            // Decodificar el token y obtener el subject (ID del usuario)
            DecodedJWT decodedJWT = JWT.decode(token);
            String userId = decodedJWT.getSubject();

            List<RecetaEntity> recetas = recetaService.todasLasRecetasPorUsuarioId(Long.parseLong(userId));
            return new ResponseEntity<>(recetas, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            // Mostrar el error en la consola del backend
            System.err.println("Error: " + e.getMessage());
            // Enviar el mensaje de error al frontend
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Ocurrió un error inesperado. Por favor, inténtalo más tarde.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @DeleteMapping("/receta/{id}")
    public ResponseEntity<?> eliminarRecetaPorId(@PathVariable("id") Long id, HttpServletRequest request) {
        try {
            // Obtener el token del encabezado Authorization
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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
            // Validar autenticación
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new ResponseEntity<>("No autorizado", HttpStatus.UNAUTHORIZED);
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
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return new ResponseEntity<>("No autorizado", HttpStatus.UNAUTHORIZED);
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

}
