package com.backend.controlador;

import java.util.Date;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.backend.jwt.JwtBlacklistService;
import com.backend.jwt.JwtTokenProvider;
import com.backend.modelos.UsuarioEntity;
import com.backend.modelos.dto.ValidationErrorResponse;
import com.backend.repositorio.UsuarioRepositorio;
import com.backend.servicio.UsuarioServicio;
import com.shared.modelos.LoginDTO;
import com.shared.modelos.RegisterDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UsuarioServicio usuarioServicio;
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder; // Para cifrar contraseñas
    private final JwtTokenProvider jwtTokenProvider; // Para generar el JWT
    private final JwtBlacklistService jwtBlacklistService; // Para el logout

    // Inyección de dependencias a través del constructor
    public AuthController(AuthenticationManager authenticationManager,
            UsuarioRepositorio usuarioRepositorio,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            JwtBlacklistService jwtBlacklistService,
            UsuarioServicio usuarioServicio) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtBlacklistService = jwtBlacklistService;
        this.usuarioServicio = usuarioServicio;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginDTO dto) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getCorreo(),
                            dto.getContrasena()));
            // Si la autenticación es exitosa, establecer la autenticación en el contexto de
            // seguridad
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generar el token JWT para el usuario autenticado
            Optional<UsuarioEntity> usOptional = usuarioRepositorio.findByCorreo(authentication.getName());

            if (usOptional.isEmpty()) {
                // Esto no debería pasar si la autenticación fue exitosa
                return new ResponseEntity<>("Usuario no encontrado", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            UsuarioEntity authenticatedUser = usOptional.get();

            // Generar el token JWT, pasando el correo, ID y nombre
            String token = jwtTokenProvider.generateToken(
                    authenticatedUser.getId(),
                    authenticatedUser.getNombre(),
                    authenticatedUser.getCorreo());
            return new ResponseEntity<>(token, HttpStatus.OK);
        } catch (org.springframework.security.core.AuthenticationException e) {
            // Capturar la excepción de autenticación para dar un mensaje más específico
            log.error("Login fallido para usuario {}: {}", dto.getCorreo(), e.getMessage());
            return new ResponseEntity<>("Correo o contraseña incorrecto.", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegisterDTO dto) {
        try {
            if (usuarioRepositorio.findByCorreo(dto.getCorreo()).isPresent()) {
                return new ResponseEntity<>("El correo ya esta en uso", HttpStatus.BAD_REQUEST);
            }
            UsuarioEntity usuarioGuardar = UsuarioEntity.builder()
                    .nombre(dto.getNombre())
                    .apellido(dto.getApellido())
                    .correo(dto.getCorreo())
                    .contrasena(passwordEncoder.encode(dto.getContrasena()))
                    .fechaNac(dto.getFechaNac())
                    .build();

            usuarioServicio.crearUsuario(usuarioGuardar);
            return ResponseEntity.ok("Usuario registrado exitosamente");

        } catch (Exception e) {
            ValidationErrorResponse errorResponse = new ValidationErrorResponse();
            errorResponse.addError("Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String jwt = getJwtFromRequest(request);

        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
            Date expiration = jwtTokenProvider.getExpirationDateFromJWT(jwt);
            long expiresInMillis = expiration.getTime() - System.currentTimeMillis();

            // Añadir el token a la lista negra por el tiempo restante hasta su expiración
            jwtBlacklistService.blacklistToken(jwt, expiresInMillis);
            return ResponseEntity.ok("Logout successful.");
        }
        return ResponseEntity.badRequest().body("Invalid JWT token or no token provided.");
    }

    // Método auxiliar para extraer el JWT de la solicitud
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
