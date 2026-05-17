package com.example.appredcicla.models;

public class Ayudante extends Empleado {
    private String licenciaConducir;

    public Ayudante(String nombrecompleto, String rut, String correo, String telefono, String claveAcceso, String licenciaConducir) {
        super(nombrecompleto, rut, correo, telefono, claveAcceso);
        this.licenciaConducir = licenciaConducir;
    }

    public void setLicenciaConducir(String licenciaConducir) {
        this.licenciaConducir = licenciaConducir;
    }
    public String getLicenciaConducir() {
        return licenciaConducir;
    }
}