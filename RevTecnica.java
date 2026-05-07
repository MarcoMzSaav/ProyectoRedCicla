import java.util.Date;

public class RevTecnica {

    private Date fechaRevision;
    private boolean aprobada;
    private String observaciones;
    private Date fechaVencimiento;

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