private class Ruta{
    private String nombre;
    private ArrayList<PuntoReciclaje> puntosRec;

    public Ruta(String nombre) {
        this.nombre = nombre;
        this.puntosRec = new ArrayList<>();
    }

    public void agregarPuntoReciclaje(PuntoReciclaje punto) {
        puntosRec.add(punto);
    }

    public String getNombre() {
        return nombre;
    }

    public ArrayList<PuntoReciclaje> getPuntosRec() {
        return puntosRec;
    }

}