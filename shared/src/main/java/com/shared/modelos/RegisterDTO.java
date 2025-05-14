package com.shared.modelos;

import java.time.LocalDate;

public class RegisterDTO {
    private String nombre;
    private String apellido;
    private String correo;
    private LocalDate fechaNac;
    private String contrasena;

    // Constructor privado para el Builder
    private RegisterDTO() {
    }

    public static Builder builder() {
        return new Builder();
    }

    // Clase Builder estática
    public static class Builder {
        private RegisterDTO dto;

        private Builder() {
            dto = new RegisterDTO();
        }

        public Builder nombre(String nombre) {
            dto.nombre = nombre;
            return this;
        }

        public Builder apellido(String apellido) {
            dto.apellido = apellido;
            return this;
        }

        public Builder correo(String correo) {
            dto.correo = correo;
            return this;
        }

        public Builder fechaNac(LocalDate fechaNac) {
            dto.fechaNac = fechaNac;
            return this;
        }

        public Builder contrasena(String contrasena) {
            dto.contrasena = contrasena;
            return this;
        }

        public RegisterDTO build() {
            return dto;
        }
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

    public LocalDate getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(LocalDate fechaNac) {
        this.fechaNac = fechaNac;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

}
