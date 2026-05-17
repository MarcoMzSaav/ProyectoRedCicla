package com.example.appredcicla.models;

public class Conductor extends Empleado {
    private String licenciaConducir;

    public Conductor(String nombrecompleto, String rut, String correo, String telefono, String claveAcceso, String licenciaConducir) {
        super(nombrecompleto, rut, correo, telefono, claveAcceso);
        this.licenciaConducir = licenciaConducir;
    }

    public String getLicenciaConducir() {
        return licenciaConducir;
    }
    public void setLicenciaConducir(String licenciaConducir) {
        this.licenciaConducir = licenciaConducir;
    }

}