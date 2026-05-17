import java.util.ArrayList;
import java.util.Date;

public class PuntoReciclaje {
    private int id;
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<Contenedor> contenedores;
    private String direccion;
    private double latitud;
    private double longitud;
    private int intervaloRecoleccion; // en dias 
    private Date fechaUltimaRecoleccion;
    private boolean estado; // true si el punto de reciclaje está en buen estado, false si requiere mantenimiento

    public PuntoReciclaje(String direccion, double latitud, double longitud) {
        this.id = 0;
        this.direccion = direccion;
        this.contenedores = new ArrayList<>();
        this.latitud = latitud;
        this.longitud = longitud;
        this.intervaloRecoleccion = 7; // Valor predeterminado, se puede modificar según las necesidades
    }

    public void agregarRegistroContenedor(int indiceContenedor, float registro) {
        if (indiceContenedor >= 0 && indiceContenedor < contenedores.size()) {
            contenedores.get(indiceContenedor).agregarRegistro(registro);
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }
    public void setIntervaloRecoleccion(int intervaloRecoleccion) {
        this.intervaloRecoleccion = intervaloRecoleccion;
    }

    public void crearContenedor(float capacidad) {
        Contenedor nuevoContenedor = new Contenedor(capacidad);
        contenedores.add(nuevoContenedor);
    }
    
    public void eliminarContenedor(int indice) {
        if (indice >= 0 && indice < contenedores.size()) {
            contenedores.remove(indice);
        }
    }
    public void registrarRecoleccion() {
        this.fechaUltimaRecoleccion = new Date(); // Registra la fecha actual como la última recolección
    }

    public void actualizarEstadoPunto(boolean estado) {
        this.estado = estado;
    }

    public String getDireccion() {
        return direccion;
    }

    public ArrayList<Contenedor> getContenedores() {
        return contenedores;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public int getIntervaloRecoleccion() {
        return intervaloRecoleccion;
    }

    public Date getFechaUltimaRecoleccion() {
        return fechaUltimaRecoleccion;
    }

    public boolean getEstado() {
        return estado;
    }
}