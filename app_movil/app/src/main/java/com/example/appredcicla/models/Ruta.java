package com.example.appredcicla.models;

import java.util.ArrayList;

public class Ruta{
    private String nombre;
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<PuntoReciclaje> puntosRec;

    public Ruta(String nombre) {
        this.nombre = nombre;
        this.puntosRec = new ArrayList<>();
    }

    public void agregarPuntoReciclaje(PuntoReciclaje punto) {
        puntosRec.add(punto);
    }

    public void eliminarPuntoReciclaje(int id) {
        for (int i = 0; i < puntosRec.size(); i++) {
            if (puntosRec.get(i).getId() == id) {
                puntosRec.remove(i);
                break;
            }
        }
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public ArrayList<PuntoReciclaje> getPuntosRec() {
        return puntosRec;
    }

    public PuntoReciclaje getPuntoReciclajeById(int id) {
        for (PuntoReciclaje punto : puntosRec) {
            if (punto.getId() == id) {
                return punto;
            }
        }
        return null; // Retorna null si no se encuentra el punto de reciclaje con el ID especificado
    }

}