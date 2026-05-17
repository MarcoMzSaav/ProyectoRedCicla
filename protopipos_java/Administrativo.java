public class Administrativo extends Empleado {

    public String area;

    public Administrativo(String nombrecompleto, String rut, String correo, String telefono, String claveAcceso, String area) {
        super(nombrecompleto, rut, correo, telefono, claveAcceso);
        this.area = area;
    }

    public String getArea() {
        return area;
    }
    public void setArea(String area) {
        this.area = area;
    }

}