package org.example.escritorio.sesion;

/**
 * Datos del empleado logueado, a mano para las pantallas.
 * Hay una sola sesión por ejecución. La clave no se guarda.
 *
 * Varios services del backend piden el usuario como parámetro, por ejemplo:
 *   envioService.actualizarEstado(id, estado, Sesion.usuario(), Sesion.esAdmin())
 */
public final class Sesion {

    private static volatile String usuario;
    private static volatile String nombreCompleto;
    private static volatile Rol rol;
    private static volatile Integer legajo;
    private static volatile int nroRestaurante;

    private Sesion() {
    }

    /** Solo lo llama AutenticacionService, después de verificar la clave. */
    static void iniciar(String usuarioLogueado, String nombre, Rol rolDelEmpleado, Integer legajoDelEmpleado,
                        int restauranteDelEmpleado) {
        usuario = usuarioLogueado;
        nombreCompleto = nombre;
        rol = rolDelEmpleado;
        legajo = legajoDelEmpleado;
        nroRestaurante = restauranteDelEmpleado;
    }

    public static void cerrar() {
        usuario = null;
        nombreCompleto = null;
        rol = null;
        legajo = null;
        nroRestaurante = 0;
    }

    public static boolean estaIniciada() {
        return usuario != null;
    }

    public static String usuario() {
        return usuario;
    }

    public static String nombreCompleto() {
        return nombreCompleto == null ? "" : nombreCompleto;
    }

    public static Rol rol() {
        return rol;
    }

    public static Integer legajo() {
        return legajo;
    }

    /** Restaurante del sector del empleado (lo piden, por ejemplo, el walk-in y el cierre de caja). */
    public static int nroRestaurante() {
        return nroRestaurante;
    }

    public static boolean esAdmin() {
        return rol == Rol.ADMIN;
    }
}