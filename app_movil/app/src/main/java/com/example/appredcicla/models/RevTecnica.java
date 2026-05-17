package com.example.appredcicla.models;
import java.util.Date;

public class RevTecnica{

    private final Date fechaRevision;
    private final boolean aprobada;
    private final String observaciones;
    private final Date fechaVencimiento;

    public RevTecnica(Date fechaRevision, boolean aprobada, String observaciones, Date fechaVencimiento) {
        this.fechaRevision = fechaRevision;
        this.aprobada = aprobada;
        this.observaciones = observaciones;
        this.fechaVencimiento = fechaVencimiento;
    }

    public Date getFechaRevision() {
        return fechaRevision;
    }

    public boolean isAprobada() {
        return aprobada;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }
}