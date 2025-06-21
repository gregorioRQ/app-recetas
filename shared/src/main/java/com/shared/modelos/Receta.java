package com.shared.modelos;

import java.time.LocalTime;

public class Receta {
    private Long id;
    // titulo o nombre de la receta
    private String nombre;
    private String ingredientes;
    private String instrucciones;
    // id del usuario que creo la receta
    private Long creadorId;
    private LocalTime tiempoPreparacion;
    private LocalTime tiempoCoccion;
    private Integer porciones;
    private String pathImg;

    public Receta() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Receta receta;

        public Builder() {
            receta = new Receta();
        }

        public Builder id(Long id) {
            receta.id = id;
            return this;
        }

        public Builder nombre(String nombre) {
            receta.nombre = nombre;
            return this;
        }

        public Builder ingredientes(String ingredientes) {
            receta.ingredientes = ingredientes;
            return this;
        }

        public Builder instrucciones(String instrucciones) {
            receta.instrucciones = instrucciones;
            return this;
        }

        public Builder creadorId(Long creadorId) {
            receta.creadorId = creadorId;
            return this;
        }

        public Builder tiempoPreparacion(LocalTime tiempoPreparacion) {
            receta.tiempoPreparacion = tiempoPreparacion;
            return this;
        }

        public Builder tiempoCoccion(LocalTime tiempoCoccion) {
            receta.tiempoCoccion = tiempoCoccion;
            return this;
        }

        public Builder porciones(Integer porciones) {
            receta.porciones = porciones;
            return this;
        }

        public Builder pathImg(String pathImg) {
            receta.pathImg = pathImg;
            return this;
        }

        public Receta build() {
            return receta;
        }
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

    public LocalTime getTiempoPreparacion() {
        return tiempoPreparacion;
    }

    public void setTiempoPreparacion(LocalTime tiempoPreparacion) {
        this.tiempoPreparacion = tiempoPreparacion;
    }

    public LocalTime getTiempoCoccion() {
        return tiempoCoccion;
    }

    public void setTiempoCoccion(LocalTime tiempoCoccion) {
        this.tiempoCoccion = tiempoCoccion;
    }

    public Integer getPorciones() {
        return porciones;
    }

    public void setPorciones(Integer porciones) {
        this.porciones = porciones;
    }

    public String getPathImg() {
        return pathImg;
    }

    public void setPathImg(String pathImg) {
        this.pathImg = pathImg;
    }

    @Override
    public String toString() {
        return "Receta [id=" + id + ", nombre=" + nombre + ", ingredientes=" + ingredientes + ", instrucciones="
                + instrucciones + ", creadorId=" + creadorId + ", tiempoPreparacion=" + tiempoPreparacion
                + ", tiempoCoccion=" + tiempoCoccion + ", porciones=" + porciones + ", pathImg=" + pathImg + "]";
    }

}
