package com.backend.servicio;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.modelos.UsuarioEntity;
import com.backend.repositorio.UsuarioRepositorio;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UsuarioServicio {
    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Transactional
    public UsuarioEntity crearUsuario(UsuarioEntity usuarioEntity) {

        UsuarioEntity usuarioSave = UsuarioEntity.builder()
                .nombre(usuarioEntity.getNombre())
                .apellido(usuarioEntity.getApellido())
                .fechaNac(usuarioEntity.getFechaNac())
                .correo(usuarioEntity.getCorreo())
                .contrasena(usuarioEntity.getContrasena())
                .build();
        usuarioRepositorio.save(usuarioSave);
        return usuarioSave;
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Optional<UsuarioEntity> usuarioOptional = usuarioRepositorio.findById(id);
        if (usuarioOptional.isEmpty()) {
            throw new EntityNotFoundException("El usuario no existe");
        }
        usuarioRepositorio.deleteById(id);
    }
}
