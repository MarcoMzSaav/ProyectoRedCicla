public class Contenedor {
    private float capacidad;
    private boolean estado;
    private ArrayList<Integer> historial;


    public Contenedor(float capacidad) {
        this.capacidad = capacidad;
        this.estado = true; // true cuando esta disponible, false cuando tiene algun problema o requiere revision
        this.historial = new ArrayList<>();
    }

    public void agregarRegistro(float registro) {
        historial.add((float) registro);
    }

    public float getCapacidad() {
        return capacidad;
    }

    public boolean getEstado() {
        return estado;
    }
}