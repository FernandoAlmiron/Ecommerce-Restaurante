package org.example.escritorio.sesion;

/**
 * Los roles que asigna el backend (CustomUserDetailsService) según el sector del empleado.
 * Acá no se repite la regla sector → rol: el rol se lee de lo que devuelve el backend ("ROLE_CAJA").
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

    /** Convierte la autoridad de Spring Security ("ROLE_CAJA") en el rol. Si no la reconoce, EMPLEADO. */
    public static Rol desdeAutoridad(String autoridad) {
        if (autoridad == null || !autoridad.startsWith("ROLE_")) {
            return EMPLEADO;
        }
        try {
            return valueOf(autoridad.substring("ROLE_".length()));
        } catch (IllegalArgumentException e) {
            return EMPLEADO;
        }
    }
}