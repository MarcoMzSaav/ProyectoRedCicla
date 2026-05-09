public class Zona{
    private String nombre;
    private ArrayList<PuntoReciclaje> puntosRec;
    private ArrayList<Ruta> rutas;

    public Zona(String nombre) {
        this.nombre = nombre;
        this.puntosRec = new ArrayList<>();
        this.rutas = new ArrayList<>();
    }
    
}