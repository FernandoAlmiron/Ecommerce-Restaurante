package org.example.escritorio.vista.rrhh;

import org.example.Modelo.persona.Empleado;
import org.example.escritorio.vista.comun.ModeloTablaBase;
import org.example.escritorio.vista.comun.Tablas;

import java.text.NumberFormat;
import java.util.Locale;

/** Columnas de la lista de empleados. La clave nunca se muestra. */
public class EmpleadoTableModel extends ModeloTablaBase<Empleado> {

    public static final int COL_LEGAJO = 0;
    public static final int COL_APELLIDO = 1;
    public static final int COL_SECTOR = 5;

    public EmpleadoTableModel() {
        super("Legajo", "Empleado", "DNI", "Celular", "Usuario", "Sector");
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

    /** El nombre del sector como está en la base (SALON, RECURSOSHUMANOS...). */
    public static String sector(Empleado e) {
        return e.getSector() == null ? "" : e.getSector().getNombre();
    }

    /** Solo ADMIN puede tocar empleados de ADMINISTRACION (RF-55). */
    public static boolean esAdministracion(Empleado e) {
        return "ADMINISTRACION".equalsIgnoreCase(sector(e));
    }

    /** 34111003 → 34.111.003 */
    public static String dni(int dni) {
        return dni == 0 ? "—" : NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-AR")).format(dni);
    }

    /** SALON → Salón, RECURSOSHUMANOS → Recursos humanos. Los que no se conocen, con mayúscula inicial. */
    public static String textoSector(String sector) {
        if (sector == null || sector.isBlank()) {
            return "Sin sector";
        }
        return switch (sector.toUpperCase(Locale.ROOT)) {
            case "ADMINISTRACION" -> "Administración";
            case "RECURSOSHUMANOS" -> "Recursos humanos";
            case "SALON" -> "Salón";
            case "RECEPCION" -> "Recepción";
            case "REPARTIDOR" -> "Reparto";
            case "COCINA" -> "Cocina";
            case "CAJA" -> "Caja";
            case "BARRA" -> "Barra";
            default -> sector.charAt(0) + sector.substring(1).toLowerCase(Locale.ROOT);
        };
    }

    /** Cada sector con su color, así se distinguen de un vistazo en la lista. */
    public static Tablas.Insignia insigniaSector(String sector) {
        String texto = textoSector(sector);
        if (sector == null) {
            return Tablas.Insignia.gris(texto);
        }
        return switch (sector.toUpperCase(Locale.ROOT)) {
            case "SALON", "REPARTIDOR" -> Tablas.Insignia.verde(texto);
            case "COCINA", "RECEPCION" -> Tablas.Insignia.amarilla(texto);
            case "CAJA" -> Tablas.Insignia.azul(texto);
            case "RECURSOSHUMANOS" -> Tablas.Insignia.violeta(texto);
            case "BARRA" -> Tablas.Insignia.roja(texto);
            default -> Tablas.Insignia.gris(texto);
        };
    }
}
