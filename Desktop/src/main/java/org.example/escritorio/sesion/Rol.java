package org.example.escritorio.sesion;

/**
 * Rol de quien inicia sesión. El de un empleado sale de su sector.
 * Cuando se separe el backend, este enum se mueve a restaurante-core (Modelo/enums)
 * y lo usan también el servidor y la web.
 */
public enum Rol {
    ADMIN("Administración"),
    RECEPCION("Recepción"),
    MOZO("Mozo"),
    COCINA("Cocina"),
    CAJA("Caja"),
    REPARTIDOR("Repartidor"),
    RRHH("Recursos humanos"),
    CLIENTE("Cliente"),
    EMPLEADO("Empleado sin sector");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String descripcion() {
        return descripcion;
    }

    /** Solo los empleados con un sector conocido pueden usar la app de escritorio. */
    public boolean puedeUsarEscritorio() {
        return this != CLIENTE && this != EMPLEADO;
    }

    /** La misma regla que usa el servidor para pasar de sector a rol. */
    public static Rol desdeSector(String sector) {
        if (sector == null) {
            return EMPLEADO;
        }
        return switch (sector.toUpperCase()) {
            case "ADMINISTRACION" -> ADMIN;
            case "COCINA" -> COCINA;
            case "SALON", "BARRA" -> MOZO;
            case "CAJA" -> CAJA;
            case "REPARTIDOR" -> REPARTIDOR;
            case "RECEPCION" -> RECEPCION;
            case "RECURSOSHUMANOS" -> RRHH;
            default -> EMPLEADO;
        };
    }
}