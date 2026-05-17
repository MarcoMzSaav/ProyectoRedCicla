import java.util.Date;

public class RegistroDeRetiro{
    private final PuntoReciclaje puntoReciclaje;
    private final Date fechaHoraRetiro;
    private float cantidadRetirada;
    private String rutaIMGAntes;
    private String rutaIMGDespues;

    public RegistroDeRetiro(PuntoReciclaje puntoReciclaje, Date fechaHoraRetiro) {
        this.puntoReciclaje = puntoReciclaje;
        this.fechaHoraRetiro = fechaHoraRetiro;
    }

    public void cantidadContenedor(int numeroContenedor, float cantidadRetirada) {
        puntoReciclaje.agregarRegistroContenedor(numeroContenedor, cantidadRetirada);
        this.cantidadRetirada += cantidadRetirada;
    }

    public Date getFechaHoraRetiro() {
        return fechaHoraRetiro;
    }

    public void setRutaIMGAntes(String rutaIMGAntes) {
        this.rutaIMGAntes = rutaIMGAntes;
    }

    public void setRutaIMGDespues(String rutaIMGDespues) {
        this.rutaIMGDespues = rutaIMGDespues;
    }

    public float getCantidadRetirada() {
        return cantidadRetirada;
    }

    public String getRutaIMGAntes() {
        return rutaIMGAntes;
    }
    public String getRutaIMGDespues() {
        return rutaIMGDespues;
    }
}