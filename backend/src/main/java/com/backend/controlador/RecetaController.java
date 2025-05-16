package com.backend.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.servicio.RecetaService;
import com.shared.modelos.Receta;

@RestController
@RequestMapping("/api/v1")
public class RecetaController {
    @Autowired
    private RecetaService recetaService;

    @PostMapping("/receta")
    public ResponseEntity<String> crearReceta(@RequestBody Receta receta) {
        recetaService.guardarReceta(receta);
        return new ResponseEntity<>("Receta creada.", HttpStatus.CREATED);
    }
}
