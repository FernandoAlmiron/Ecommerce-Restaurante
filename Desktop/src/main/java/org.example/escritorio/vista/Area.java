package org.example.escritorio.vista;

import org.example.escritorio.sesion.Rol;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Cada opción del menú de la ventana principal y qué roles la ven.
 * ADMIN ve todas. Si más adelante un área la tiene que ver otro rol, se agrega en su lista.
 */
public enum Area {
    RECEPCION("Recepción", "S1", Rol.RECEPCION),
    SALON("Salón", "S2", Rol.MOZO),
    COCINA("Cocina", "S3", Rol.COCINA),
    CAJA("Caja", "S4", Rol.CAJA),
    REPARTO("Reparto", "S5", Rol.REPARTIDOR),
    RRHH("Recursos humanos", "S6", Rol.RRHH),
    ADMINISTRACION("Administración", "S7", Rol.ADMIN);

    private final String titulo;
    private final String fase;
    private final Set<Rol> roles;

    Area(String titulo, String fase, Rol... roles) {
        this.titulo = titulo;
        this.fase = fase;
        this.roles = EnumSet.copyOf(Arrays.asList(roles));
    }

    public String titulo() {
        return titulo;
    }

    /** Fase de la guía en la que se arma la pantalla de esta área. */
    public String fase() {
        return fase;
    }

    public boolean permitidaPara(Rol rol) {
        return rol == Rol.ADMIN || roles.contains(rol);
    }

    public static List<Area> permitidasPara(Rol rol) {
        return Arrays.stream(values()).filter(a -> a.permitidaPara(rol)).toList();
    }
}