package com.backend.servicio;

import java.util.ArrayList;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.backend.modelos.UsuarioEntity;
import com.backend.repositorio.UsuarioRepositorio;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepositorio usuarioRepo;

    public CustomUserDetailsService(UsuarioRepositorio usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        UsuarioEntity usuarioOP = usuarioRepo.findByCorreo(correo)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con el correo: " + correo));

        return new org.springframework.security.core.userdetails.User(
                usuarioOP.getCorreo(), usuarioOP.getContrasena(), new ArrayList<>());
    }

}
