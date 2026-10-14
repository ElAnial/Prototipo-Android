package com.example.sqlite;

import androidx.annotation.NonNull;

public class Rol {
    private int idRol;
    private String nombreRol;
    private String descripcion;

    public Rol() {}

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }

    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    // El Spinner de roles usa este texto para mostrar cada opción
    @NonNull
    @Override
    public String toString() {
        return nombreRol;
    }
}