package com.backend.modelos;

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

@Data
@Entity
@Table(name = "recetas")
public class RecetaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // titulo o nombre de la receta
    @Column(nullable = false, length = 20, unique = true)
    private String nombre;
    @Column(nullable = false, length = 200)
    private String ingredientes;
    @Column(nullable = false, length = 800)
    private String instrucciones;
    // id del usuario que creo la receta
    private Long creadorId;

    @Column(nullable = false, length = 20)
    private String tiempoPreparacion;
    @Column(nullable = false, length = 200)
    private String tiempoCoccion;
    @Column(nullable = false, length = 20)
    private Integer porciones;

    // Relación ManyToOne inversa
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;
}
