package com.example.appredcicla.models;

import java.util.ArrayList;
import java.util.Date;    

public class Camion {
    private final String patente;
    private final float capacidadCarga;
    private Date ultimaRevision;
    private boolean estado; // true si el camión está disponible, false si está en mantenimiento o no puede ser utilizado
    private boolean alerta;
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<RevTecnica> registroRevisiones;

    public Camion(String patente, float capacidadCarga) {
        this.patente = patente;
        this.capacidadCarga = capacidadCarga;
        this.estado = true; // El camión se considera disponible al ser creado
        this.alerta = false; // No hay alerta al ser creado
        this.registroRevisiones = new ArrayList<>();
    }

    public void agregarRevision(RevTecnica revision) {
        this.registroRevisiones.add(revision);
        this.ultimaRevision = revision.getFechaRevision();
        if (!revision.isAprobada()) {
            this.estado = false; // El camión no está disponible si la revisión no fue aprobada
            this.alerta = true; // Se activa la alerta si la revisión no fue aprobada
        }
    }

    public String getPatente() {
        return patente;
    }

    public float getCapacidadCarga() {
        return capacidadCarga;
    }

    public boolean isDisponible() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public boolean hasAlerta() {
        return alerta;
    }

    public void setAlerta(boolean alerta) {
        this.alerta = alerta;
    }

    public ArrayList<RevTecnica> getRegistroRevisiones() {
        return registroRevisiones;
    }

    public Date getUltimaRevision() {
        return ultimaRevision;
    }
}