package com.backend.servicio;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.modelos.RecetaEntity;
import com.backend.modelos.UsuarioEntity;
import com.backend.repositorio.RecetaRepositorio;
import com.backend.repositorio.UsuarioRepositorio;
import com.shared.modelos.Receta;

import jakarta.persistence.EntityNotFoundException;

@Service
public class RecetaService {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private RecetaRepositorio recetaRepositorio;

    public void guardarReceta(Receta receta) {
        Optional<UsuarioEntity> usuarioOp = usuarioRepositorio.findById(receta.getCreadorId());

        UsuarioEntity usuario = usuarioOp.get();
        if (usuario == null) {
            throw new EntityNotFoundException("Usuario no encontrado");
        }

        RecetaEntity recetaGuardar = new RecetaEntity();
        recetaGuardar.setNombre(receta.getNombre());
        recetaGuardar.setIngredientes(receta.getIngredientes());
        recetaGuardar.setInstrucciones(receta.getInstrucciones());
        recetaGuardar.setTiempoPreparacion(receta.getTiempoPreparacion());
        recetaGuardar.setTiempoCoccion(receta.getTiempoCoccion());
        recetaGuardar.setPorciones(receta.getPorciones());
        recetaGuardar.setUsuario(usuario);

        recetaRepositorio.save(recetaGuardar);
        System.out.println(recetaGuardar.toString());

    }

    public List<RecetaEntity> todasLasRecetasPorUsuarioId(Long userId) {

        Optional<UsuarioEntity> usuarioOp = usuarioRepositorio.findById(userId);

        if (usuarioOp.isEmpty()) {
            throw new EntityNotFoundException("Usuario con ID " + userId + " no encontrado.");
        }
        return recetaRepositorio.findByUsuarioId(userId);
    }

    public void eliminarRecetaPorId(Long id) {
        recetaRepositorio.deleteById(id);
    }
}
