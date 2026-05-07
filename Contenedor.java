public class Contenedor {
    private float capacidad;
    private boolean estado;
    private ArrayList<Integer> historial;


    private Contenedor(float capacidad) {
        this.capacidad = capacidad;
        this.estado = true; // true cuando esta disponible, false cuando tiene algun problema o requiere revision
        this.historial = new ArrayList<>();
    }

    public void agregarRegistro(float registro) {
        if (historial.size() >= 10) {
            historial.remove(0); // Elimina el registro más antiguo si se supera el límite de 10
        }
        historial.add((float) registro);
    }

    public ArrayList<Integer> getHistorial() {
        return historial;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public float getCapacidad() {
        return capacidad;
    }

    public boolean getEstado() {
        return estado;
    }
}