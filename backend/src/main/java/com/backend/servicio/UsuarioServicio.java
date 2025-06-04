package com.backend.servicio;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.modelos.UsuarioEntity;
import com.backend.modelos.dto.UsuarioDTO;
import com.backend.modelos.dto.ValidationErrorResponse;
import com.backend.modelos.dto.ValidationException;
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

        ValidationErrorResponse validacion = validarUsuario(usuarioEntity);
        if (validacion.hasErrors()) {
            throw new ValidationException("Errores de validación", validacion);
        }

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

    private ValidationErrorResponse validarUsuario(UsuarioEntity usuario) {
        ValidationErrorResponse validacion = new ValidationErrorResponse();

        // Validar campos obligatorios
        if (!StringUtils.hasText(usuario.getNombre())) {
            validacion.addError("El nombre es requerido");
        }
        if (!StringUtils.hasText(usuario.getApellido())) {
            validacion.addError("El apellido es requerido");
        }
        if (!StringUtils.hasText(usuario.getCorreo())) {
            validacion.addError("El correo es requerido");
        }
        if (!StringUtils.hasText(usuario.getContrasena())) {
            validacion.addError("La contraseña es requerida");
        }
        if (usuario.getFechaNac() == null) {
            validacion.addError("La fecha de nacimiento es requerida");
        }

        // Validar si el usuario existe
        if (StringUtils.hasText(usuario.getCorreo()) &&
                usuarioRepositorio.findByCorreo(usuario.getCorreo()).isPresent()) {
            validacion.addError("Este correo ya está registrado");
        }

        return validacion;
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
