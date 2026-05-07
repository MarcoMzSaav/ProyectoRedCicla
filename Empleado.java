
public abstract class Empleado {
    private String nombrecompleto;
    private String rut;
    private String correo;
    private String telefono;
    private String claveAcceso;


    public Empleado(String nombrecompleto, String rut, String correo, String telefono, String claveAcceso) {
        this.nombrecompleto = nombrecompleto;
        this.rut = rut;
        this.correo = correo;
        this.telefono = telefono;
        this.claveAcceso = claveAcceso;
    }

    public String getNombreCompleto() {
        return nombrecompleto;
    }

    public String getRut() {
        return rut;
    }

    public String getCorreoElectronico() {
        return correo;
    }

    public String getNumeroTelefono() {
        return telefono;
    }
}