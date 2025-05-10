package com.shared.modelos;

import java.util.Date;

public class Usuario {
    private Long id;
    private String nombre;
    private String apellido;
    private String correo;
    private Date fechaNac;
    private String contrasena;

    public Usuario() {
    }

    public Usuario(Long id, String nombre, String apellido, String correo, Date fechaNac, String contrasena) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.fechaNac = fechaNac;
        this.contrasena = contrasena;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Date getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(Date fechaNac) {
        this.fechaNac = fechaNac;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    @Override
    public String toString() {
        return "Usuario [id=" + id + ", nombre=" + nombre + ", apellido=" + apellido + ", correo=" + correo
                + ", fechaNac=" + fechaNac + ", contrasena=" + contrasena + "]";
    }

}
