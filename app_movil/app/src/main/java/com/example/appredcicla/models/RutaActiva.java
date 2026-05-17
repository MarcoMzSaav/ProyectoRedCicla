package com.example.appredcicla.models;

import java.util.ArrayList;
import java.util.Date;
public class RutaActiva {
    private final Ruta ruta;
    private final Date fechaHoraInicio;
    private Date fechaHoraFin;
    private final Conductor conductor;
    private final Ayudante ayudante;
    private final Camion camion;
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<RegistroDeRetiro> registrosDeRetiro;
    private float pesajeCalculado;
    private float pesajeFinal;
    

    public RutaActiva(Ruta ruta, Conductor conductor, Ayudante ayudante, Camion camion, Date fechaHoraInicio) {
        this.ruta = ruta;
        this.conductor = conductor;
        this.ayudante = ayudante;
        this.camion = camion;
        this.fechaHoraInicio = fechaHoraInicio;
        this.registrosDeRetiro = new ArrayList<>();
    }

    public Ruta getRuta() {
        return ruta;
    }

    public Conductor getConductor() {
        return conductor;
    }

    public Ayudante getAyudante() {
        return ayudante;
    }
    
    public Camion getCamion() {
        return camion;
    }

    public Date getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public Date getFechaHoraFin() {
        return fechaHoraFin;
    }
    public float getPesajeCalculado() {
        return pesajeCalculado;
    }
    public float getPesajeFinal() {
        return pesajeFinal;
    }

    public void pesajeFinal(float pesajeFinal) {
        this.pesajeFinal = pesajeFinal;
    }

    public void finalizarRuta() {
        this.fechaHoraFin = new Date(); // Establece la fecha y hora de finalización al momento de llamar a este método
        float pesajecal = 0;
        for (RegistroDeRetiro registro : registrosDeRetiro) {
            pesajecal += registro.getCantidadRetirada();
        }
        this.pesajeCalculado = pesajecal;
    }

    public void crearRegistroDeRetiro(int IDpuntoReciclaje, Date fechaHoraRetiro) {
        registrosDeRetiro.add(new RegistroDeRetiro(ruta.getPuntoReciclajeById(IDpuntoReciclaje), fechaHoraRetiro));
    }

    public void agregarCantidadRetirada(int indiceRegistro, int numeroContenedor, float cantidadRetirada) {
        if (indiceRegistro >= 0 && indiceRegistro < registrosDeRetiro.size()) {
            registrosDeRetiro.get(indiceRegistro).cantidadContenedor(numeroContenedor, cantidadRetirada);
        }
    }
    public void setRutaIMGAntes(int indiceRegistro, String rutaIMGAntes) {
        if (indiceRegistro >= 0 && indiceRegistro < registrosDeRetiro.size()) {
            registrosDeRetiro.get(indiceRegistro).setRutaIMGAntes(rutaIMGAntes);
        }
    }
    public void setRutaIMGDespues(int indiceRegistro, String rutaIMGDespues) {
        if (indiceRegistro >= 0 && indiceRegistro < registrosDeRetiro.size()) {
            registrosDeRetiro.get(indiceRegistro).setRutaIMGDespues(rutaIMGDespues);
        }
    }
}
