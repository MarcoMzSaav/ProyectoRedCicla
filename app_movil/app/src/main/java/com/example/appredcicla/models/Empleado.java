package com.example.appredcicla.models;

public abstract class Empleado {
    private String nombrecompleto;
    private String rut;
    private String correo;
    private String telefono;
    private String claveAcceso;


    public Empleado(String nombrecompleto, String rut, String correo, String telefono, String claveAcceso) {
        this.nombrecompleto = nombrecompleto;
        this.rut = rut;
        this.correo = correo;
        this.telefono = telefono;
        this.claveAcceso = claveAcceso;
    }



    public String getNombreCompleto() {
        return nombrecompleto;
    }
    public void setNombreCompleto(String nombrecompleto) {
        this.nombrecompleto = nombrecompleto;
    }

    public String getRut() {
        return rut;
    }
    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getCorreoElectronico() {
        return correo;
    }
    public void setCorreoElectronico(String correo) {
        this.correo = correo;
    }

    public String getNumeroTelefono() {
        return telefono;
    }
    public void setNumeroTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getClaveAcceso() {
        return claveAcceso;
    }
    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }
}