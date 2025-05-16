package com.backend.servicio;

import org.springframework.stereotype.Service;

import com.shared.modelos.Receta;

@Service
public class RecetaService {
    public void guardarReceta(Receta receta) {
        System.out.println(
                "Receta datos: " + "creadorID: " + receta.getCreadorId() + " " + receta.getIngredientes() + " "
                        + receta.getNombre());
    }
}
