package com.shared.modelos;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public class RegisterDTO {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String apellido;

    @NotBlank(message = "El correo no puede estar vacío")
    @Size(max = 50, message = "El correo no puede exceder los 50 caracteres")
    @Email(message = "El formato del correo es inválido")
    private String correo;

    @NotNull(message = "La fecha de nacimiento no puede estar vacía")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    private LocalDate fechaNac;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 6, max = 120, message = "La contraseña debe tener entre 6 y 120")
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
