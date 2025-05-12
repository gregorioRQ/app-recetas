package com.backend.modelos.dto;

import java.util.Date;

import lombok.Data;

@Data
public class UsuarioDTO {
    private String nombre;
    private String apellido;
    private String correo;
    private Date fechaNac;
    private String contrasena;
}
