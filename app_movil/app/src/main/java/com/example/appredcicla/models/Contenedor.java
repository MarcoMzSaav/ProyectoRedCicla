package com.example.appredcicla.models;

import java.util.ArrayList;

public class Contenedor {
    private final float capacidad;
    private boolean estado;
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<Float> historial;


    public Contenedor(float capacidad) {
        this.capacidad = capacidad;
        this.estado = true; // true cuando esta disponible, false cuando tiene algun problema o requiere revision
        this.historial = new ArrayList<>();
    }

    public void agregarRegistro(float registro) {
        if (historial.size() >= 10) {
            historial.remove(0); // Elimina el registro más antiguo si se supera el límite de 10
        }
        historial.add(registro);
    }

    public ArrayList<Float> getHistorial() {
        return historial;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public float getCapacidad() {
        return capacidad;
    }

    public boolean getEstado() {
        return estado;
    }
}