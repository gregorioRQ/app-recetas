package com.backend.servicio;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
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
        RecetaEntity recetaEliminar = recetaRepositorio.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro la receta con ID: " + id));

        recetaRepositorio.deleteById(recetaEliminar.getId());
    }

    public RecetaEntity actualizarReceta(Long id, Receta recetaActualizada) {
        // Buscar la receta existente
        RecetaEntity recetaExistente = recetaRepositorio.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la receta con ID: " + id));

        // Actualizar los campos
        recetaExistente.setNombre(recetaActualizada.getNombre());
        recetaExistente.setIngredientes(recetaActualizada.getIngredientes());
        recetaExistente.setInstrucciones(recetaActualizada.getInstrucciones());
        recetaExistente.setTiempoPreparacion(recetaActualizada.getTiempoPreparacion());
        recetaExistente.setTiempoCoccion(recetaActualizada.getTiempoCoccion());
        recetaExistente.setPorciones(recetaActualizada.getPorciones());

        // Guardar y retornar la receta actualizada
        return recetaRepositorio.save(recetaExistente);
    }

    public RecetaEntity actualizarRecetaParcial(Long id, Map<String, Object> cambios) {
        RecetaEntity receta = recetaRepositorio.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontro la receta con el ID: " + id));

        cambios.forEach((key, value) -> {
            switch (key) {
                case "nombre":
                    receta.setNombre((String) value);
                    break;
                case "ingredientes":
                    receta.setIngredientes((String) value);
                    break;
                case "instrucciones":
                    receta.setInstrucciones((String) value);
                    break;
                case "tiempoPreparacion":
                    if (value instanceof Integer) {
                        receta.setTiempoPreparacion((LocalTime) value);
                    }
                    break;
                case "tiempoCoccion":
                    if (value instanceof Integer) {
                        receta.setTiempoCoccion((LocalTime) value);
                    }
                    break;
                case "porciones":
                    if (value instanceof Integer) {
                        receta.setPorciones((Integer) value);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Campo no admitido: " + key);
            }
        });
        return recetaRepositorio.save(receta);
    }

}
