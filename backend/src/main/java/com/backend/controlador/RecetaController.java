package com.backend.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.backend.modelos.RecetaEntity;
import com.backend.servicio.RecetaService;
import com.shared.modelos.Receta;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
public class RecetaController {
    @Autowired
    private RecetaService recetaService;

    @PostMapping("/receta")
    public ResponseEntity<String> crearReceta(@RequestBody Receta receta) {
        try {
            recetaService.guardarReceta(receta);
            return new ResponseEntity<>("Receta creada con éxito.", HttpStatus.CREATED);
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
}
