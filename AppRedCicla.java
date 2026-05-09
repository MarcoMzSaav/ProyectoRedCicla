import java.util.ArrayList;
import java.util.Date;

public class AppRedCicla {
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<Empleado> empleados;
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<Camion> camiones;
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<Zona> zonas;
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<RutaActiva> rutasActivas;

    public AppRedCicla() {
        this.empleados = new ArrayList<>();
        this.camiones = new ArrayList<>();
        this.zonas = new ArrayList<>();
        this.rutasActivas = new ArrayList<>();
    }

    public void crearConductor(String nombreCompleto, String rut, String correo, String telefono, String claveAcceso, String licenciaConducir) {
        Conductor nuevoConductor = new Conductor(nombreCompleto, rut, correo, telefono, claveAcceso, licenciaConducir);
        empleados.add(nuevoConductor);
    }

    public void crearAyudante(String nombreCompleto, String rut, String correo, String telefono, String claveAcceso, String licenciaConducir) {
        Ayudante nuevoAyudante = new Ayudante(nombreCompleto, rut, correo, telefono, claveAcceso, licenciaConducir);
        empleados.add(nuevoAyudante);
    }

    public void crearAdministrativo(String nombreCompleto, String rut, String correo, String telefono, String claveAcceso,String area) {
        Administrativo nuevoAdministrativo = new Administrativo(nombreCompleto, rut, correo, telefono, claveAcceso, area);
        empleados.add(nuevoAdministrativo);
    }
    public void crearCamion(String patente, float capacidadCarga) {
        Camion nuevoCamion = new Camion(patente, capacidadCarga);
        camiones.add(nuevoCamion);
    }
    public void crearZona(String nombre) {
        Zona nuevaZona = new Zona(nombre);
        zonas.add(nuevaZona);
    }

    public Zona buscarZonaPorNombre(String nombre) {
        for (Zona zona : zonas) {
            if (zona.getNombre().equalsIgnoreCase(nombre)) {
                return zona;
            }
        }
        return null; // Retorna null si no se encuentra la zona
    }

    public void crearRutaActiva(String nombreZona,String nombreRuta, Conductor conductor, Ayudante ayudante, Camion camion) {
        Zona zona = buscarZonaPorNombre(nombreZona);
        if (zona != null) {
            Ruta ruta = zona.getRutaporNombre(nombreRuta);
            if (ruta != null) {
                RutaActiva nuevaRutaActiva = new RutaActiva(ruta, conductor, ayudante, camion, new Date());
                rutasActivas.add(nuevaRutaActiva);
            }
        }
    }
}


