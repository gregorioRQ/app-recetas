package com.shared.modelos;

import java.util.Date;
import java.util.List;

public class Receta {
    private Long id;
    // titulo o nombre de la receta
    private String nombre;
    private String ingredientes;
    private String instrucciones;
    // id del usuario que creo la receta
    private Long creadorId;
    private String tiempoPreparacion;
    private String tiempoCoccion;
    private Integer porciones;

    /*
     * // Categoría de la receta (ej: "Desayuno", "Almuerzo", "Cena", "Postre",
     * // "Vegetariana", "Vegana", etc.).
     * private String categoria;
     * private Date fechaCreacion;
     * private Integer puntuacion;
     * // Lista de comentarios de los usuarios (esto probablemente sería una entidad
     * // separada relacionada).
     * private List<Comentarios> comentarios;
     * // Lista de usuarios que han marcado esta receta como favorita (esto
     * // probablemente sería una relación muchos a muchos con la tabla de
     * usuarios).
     * private List<Favoritos> favoritos;
     */

    public Receta() {
    }

    public Receta(Long id, String nombre, String ingredientes, String instrucciones, Long creadorId,
            String tiempoPreparacion, String tiempoCoccion, Integer porciones) {
        this.id = id;
        this.nombre = nombre;
        this.ingredientes = ingredientes;
        this.instrucciones = instrucciones;
        this.creadorId = creadorId;
        this.tiempoPreparacion = tiempoPreparacion;
        this.tiempoCoccion = tiempoCoccion;
        this.porciones = porciones;
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

    public String getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(String ingredientes) {
        this.ingredientes = ingredientes;
    }

    public String getInstrucciones() {
        return instrucciones;
    }

    public void setInstrucciones(String instrucciones) {
        this.instrucciones = instrucciones;
    }

    public Long getCreadorId() {
        return creadorId;
    }

    public void setCreadorId(Long creadorId) {
        this.creadorId = creadorId;
    }

    public String getTiempoPreparacion() {
        return tiempoPreparacion;
    }

    public void setTiempoPreparacion(String tiempoPreparacion) {
        this.tiempoPreparacion = tiempoPreparacion;
    }

    public String getTiempoCoccion() {
        return tiempoCoccion;
    }

    public void setTiempoCoccion(String tiempoCoccion) {
        this.tiempoCoccion = tiempoCoccion;
    }

    public Integer getPorciones() {
        return porciones;
    }

    public void setPorciones(Integer porciones) {
        this.porciones = porciones;
    }

}
