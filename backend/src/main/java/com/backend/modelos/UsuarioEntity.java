package com.backend.modelos;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.shared.modelos.Receta;
import com.shared.modelos.Usuario;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "usuarios")
public class UsuarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = false, length = 50, nullable = false)
    private String nombre;
    @Column(unique = false, length = 50, nullable = false)
    private String apellido;

    @Column(unique = true, length = 50, nullable = false)
    private String correo;

    @Column(length = 30, nullable = false)
    private LocalDate fechaNac;

    @Column(length = 120, nullable = false)
    private String contrasena;

    // Relación OneToMany bidireccional
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    // Excluir la relación para evitar recursión
    @ToString.Exclude
    @JsonManagedReference
    private List<RecetaEntity> recetas;

    // Método para agregar una receta
    public void addReceta(RecetaEntity receta) {
        recetas.add(receta);
        receta.setUsuario(this);
    }

    // Método para eliminar una receta
    public void removeReceta(RecetaEntity receta) {
        recetas.remove(receta);
        receta.setUsuario(null);
    }
}
