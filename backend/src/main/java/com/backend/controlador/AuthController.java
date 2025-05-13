package com.backend.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.modelos.UsuarioEntity;
import com.backend.servicio.UsuarioServicio;
import com.shared.modelos.RegisterDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private final UsuarioServicio usuarioServicio;

    @PostMapping("/registro")
    public ResponseEntity<String> registrarUsuario(@RequestBody RegisterDTO dto) {

        UsuarioEntity usuarioGuardar = UsuarioEntity.builder()
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .correo(dto.getCorreo())
                .contrasena(dto.getContrasena())
                .fechaNac(dto.getFechaNac())
                .build();

        usuarioServicio.crearUsuario(usuarioGuardar);
        return new ResponseEntity<>("usuario registrado", HttpStatus.OK);

    }
}
