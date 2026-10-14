package org.example.escritorio.sesion;

/**
 * Datos del empleado logueado, a mano para las pantallas.
 * Hay una sola sesión por ejecución. La clave no se guarda.
 */
public final class Sesion {

    private static volatile String usuario;
    private static volatile String nombreCompleto;
    private static volatile Rol rol;
    private static volatile Integer legajo;

    private Sesion() {
    }

    /** Solo lo llama el servicio de login, después de verificar la clave. */
    static void iniciar(String usuarioLogueado, String nombre, Rol rolDelEmpleado, Integer legajoDelEmpleado) {
        usuario = usuarioLogueado;
        nombreCompleto = nombre;
        rol = rolDelEmpleado;
        legajo = legajoDelEmpleado;
    }

    public static void cerrar() {
        usuario = null;
        nombreCompleto = null;
        rol = null;
        legajo = null;
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

    public static boolean esAdmin() {
        return rol == Rol.ADMIN;
    }
}