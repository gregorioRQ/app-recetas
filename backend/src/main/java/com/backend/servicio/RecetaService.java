package com.backend.servicio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5mb
    private static final List<String> ALLOWED_FORMATS = Arrays.asList("image/jpg", "image/jpeg", "image/png");

    @Value("${app.upload.dir:${user.home}}")
    private String uploadDir;

    public void guardarReceta(Receta receta, MultipartFile imagen) {
        Optional<UsuarioEntity> usuarioOp = usuarioRepositorio.findById(receta.getCreadorId());

        UsuarioEntity usuario = usuarioOp.get();
        if (usuario == null) {
            throw new EntityNotFoundException("Usuario no encontrado");
        }

        if (imagen != null && !imagen.isEmpty()) {
            validarImagen(imagen);
            String rutaImagen = guardarImagen(imagen);
            receta.setPathImg(rutaImagen);
        }

        RecetaEntity recetaGuardar = new RecetaEntity();
        recetaGuardar.setNombre(receta.getNombre());
        recetaGuardar.setIngredientes(receta.getIngredientes());
        recetaGuardar.setInstrucciones(receta.getInstrucciones());
        recetaGuardar.setTiempoPreparacion(receta.getTiempoPreparacion());
        recetaGuardar.setTiempoCoccion(receta.getTiempoCoccion());
        recetaGuardar.setPorciones(receta.getPorciones());
        recetaGuardar.setPathImg(receta.getPathImg());
        recetaGuardar.setUsuario(usuario);

        recetaRepositorio.save(recetaGuardar);

    }

    // metodo aux para validar la imagen
    private void validarImagen(MultipartFile imagen) {
        // validar tamaño
        if (imagen.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El tamaño del archivo exice el límite de 5MB");
        }

        // validar formato
        String contentType = imagen.getContentType();
        if (!ALLOWED_FORMATS.contains(contentType)) {
            throw new IllegalArgumentException("Formato de imagen no aceptado use en cambio: JPG, JPEG, PNG");
        }
    }

    // guarda la imagen en el sistema de archivos del servidor
    private String guardarImagen(MultipartFile imagen) {

        try {
            // nombre unico para el archivo
            String nombreArchivo = UUID.randomUUID().toString() + "_" + imagen.getOriginalFilename();

            // construir la ruta absoluta donde se guardarán las imágenes
            Path directorioImagenes = Paths.get(uploadDir, "recetas-imagenes");

            // Crear directorio si no existe
            if (!Files.exists(directorioImagenes)) {
                Files.createDirectories(directorioImagenes);
            }

            // Guardar imagen
            Path rutaCompleta = directorioImagenes.resolve(nombreArchivo);
            Files.copy(imagen.getInputStream(), rutaCompleta);

            return rutaCompleta.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen: " + e.getMessage());
        }
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
