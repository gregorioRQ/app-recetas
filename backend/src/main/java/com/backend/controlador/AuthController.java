package com.backend.controlador;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.backend.modelos.UsuarioEntity;
import com.backend.repositorio.UsuarioRepositorio;
import com.backend.servicio.UsuarioServicio;
import com.shared.modelos.RegisterDTO;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private final UsuarioServicio usuarioServicio;

    @Autowired
    private final UsuarioRepositorio usuarioRepositorio;

    // MAS ADELANTE GUARDAR LA KEY EN UNA VARIABLE DE ENTORNO
    private static final String SECRET_KEY = Dotenv.load().get("SECRET_KEY");
    // Tiempo de expiración del token (en milisegundos) - ejemplo: 1 hora
    private static final long EXPIRATION_TIME = 3_600_000;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody RegisterDTO dto) {
        String correo = dto.getCorreo();
        String contraseña = dto.getContrasena();

        // 1. Autenticar al usuario (verificar si existe y la contraseña es correcta)
        Optional<UsuarioEntity> usuarioOptional = usuarioRepositorio.findByCorreo(correo);

        if (usuarioOptional.isEmpty() || !usuarioOptional.get().getContrasena().equals(contraseña)) {
            return new ResponseEntity<>("Credenciales inválidas.", HttpStatus.UNAUTHORIZED);
        }

        UsuarioEntity usuario = usuarioOptional.get();

        // 2. Generar el token JWT si la autenticación es exitosa
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        String token = JWT.create()
                .withSubject(usuario.getId().toString()) // Identifica al usuario
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .withClaim("username", usuario.getNombre()) // Puedes incluir más información si es necesario
                .sign(algorithm);
        // 3. Por ahora, devolvemos el token en el cuerpo de la respuesta
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @PostMapping("/registro")
    public ResponseEntity<String> registrarUsuario(@RequestBody RegisterDTO dto) {

        Optional<UsuarioEntity> usuarioOptional = usuarioRepositorio.findByCorreo(dto.getCorreo());
        if (usuarioOptional.isPresent()) {
            return new ResponseEntity<>("El correo del usuario ya existe", HttpStatus.CONFLICT);
        }
        // IMPLEMENTAR UN SISTEMA PARA GUARDAR LAS CONTRASEÑA
        // ENCRIPTADAS Y NO EN TEXTO PLANO EN LA DB
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
