package com.example.appredcicla.models;

import java.util.ArrayList;

public class Zona{
    private String nombre;
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<PuntoReciclaje> puntosRec;
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<Ruta> rutas;

    public Zona(String nombre) {
        this.nombre = nombre;
        this.puntosRec = new ArrayList<>();
        this.rutas = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
            this.nombre = nombre;
    }


    public void agregarPuntoReciclaje(PuntoReciclaje punto) {
        puntosRec.add(punto);
    }
    public void eliminarPuntoReciclaje(int indice) {
        if (indice >= 0 && indice < puntosRec.size()) {
            puntosRec.remove(indice);
        }
    }

    public void agregarRuta(Ruta ruta) {
        rutas.add(ruta);
    }
    public void eliminarRuta(int indice) {
        if (indice >= 0 && indice < rutas.size()) {
            rutas.remove(indice);
        }
    }

    
    public ArrayList<PuntoReciclaje> getPuntosRec() {
        return puntosRec;
    }
    public Ruta getRutaporNombre(String nombre) {
        for (Ruta ruta : rutas) {
            if (ruta.getNombre().equals(nombre)) {
                return ruta;
            }
        }
        return null; // Retorna null si no se encuentra la ruta con el nombre especificado
    }
    public String rutasToString() {
        StringBuilder sb = new StringBuilder();
        for (Ruta ruta : rutas) {
            sb.append(ruta.getNombre()).append("\n");
        }
        return sb.toString();
    }


    public Ruta crearRuta(String nombre) {
        Ruta nuevaRuta = new Ruta(nombre);
        rutas.add(nuevaRuta);
        return nuevaRuta;
    }
    
    public void agregarPuntoARuta(String nombreRuta, PuntoReciclaje punto) {
        for (Ruta ruta : rutas) {
            if (ruta.getNombre().equals(nombreRuta)) {
                ruta.agregarPuntoReciclaje(punto);
                break;
            }
        }
    }

    public void eliminarPuntoDeRuta(String nombreRuta, int idPunto) {
        for (Ruta ruta : rutas) {
            if (ruta.getNombre().equals(nombreRuta)) {
                ruta.eliminarPuntoReciclaje(idPunto);
                break;
            }
        }
    }

}