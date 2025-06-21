package com.backend.modelos;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.shared.modelos.Receta;
import com.shared.modelos.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "recetas")
public class RecetaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // titulo o nombre de la receta
    @Column(nullable = false, length = 110, unique = true)
    private String nombre;
    @Column(nullable = false, length = 400)
    private String ingredientes;
    @Column(nullable = false, length = 4000)
    private String instrucciones;
    // id del usuario que creo la receta
    private Long creadorId;

    @Column(nullable = false)
    private LocalTime tiempoPreparacion;

    @Column(nullable = false)
    private LocalTime tiempoCoccion;
    @Column(nullable = false, length = 4)
    private Integer porciones;

    // Relación ManyToOne inversa
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    // Excluir la relación para evitar recursión
    @ToString.Exclude
    @JsonBackReference
    private UsuarioEntity usuario;

    private String pathImg;
}
