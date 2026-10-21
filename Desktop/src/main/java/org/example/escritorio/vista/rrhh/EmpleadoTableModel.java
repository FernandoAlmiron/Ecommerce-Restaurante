package org.example.escritorio.vista.rrhh;

import org.example.Modelo.persona.Empleado;
import org.example.escritorio.vista.comun.ModeloTablaBase;

/** Columnas de la lista de empleados. La clave nunca se muestra. */
public class EmpleadoTableModel extends ModeloTablaBase<Empleado> {

    public static final int COL_APELLIDO = 1;

    public EmpleadoTableModel() {
        super("Legajo", "Apellido y nombre", "DNI", "Celular", "Usuario", "Sector");
    }

    @Override
    protected Object valor(Empleado e, int columna) {
        return switch (columna) {
            case 0 -> e.getLegajo();
            case 1 -> e.getApellido() + ", " + e.getNombre();
            case 2 -> e.getDni();
            case 3 -> e.getCelular() == null ? "" : e.getCelular();
            case 4 -> e.getUsername();
            case 5 -> sector(e);
            default -> null;
        };
    }

    @Override
    protected Class<?> tipo(int columna) {
        return columna == 0 || columna == 2 ? Integer.class : String.class;
    }

    public static String sector(Empleado e) {
        return e.getSector() == null ? "" : e.getSector().getNombre();
    }

    /** Solo ADMIN puede tocar empleados de ADMINISTRACION (RF-55). */
    public static boolean esAdministracion(Empleado e) {
        return "ADMINISTRACION".equalsIgnoreCase(sector(e));
    }
}
