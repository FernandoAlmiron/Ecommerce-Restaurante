package org.example.escritorio.sesion;

/**
 * Lo que la pantalla de login necesita: verificar usuario y clave y dejar la Sesion iniciada.
 *
 * Hoy lo implementa LoginDePrueba. Cuando se separe el backend, lo implementa
 * AutenticacionService con el mismo CustomUserDetailsService y BCrypt del servidor.
 */
public interface ServicioLogin {

    /**
     * Si todo está bien, deja la Sesion iniciada.
     *
     * @throws IllegalArgumentException si el usuario o la clave son incorrectos
     * @throws IllegalStateException    si el usuario existe pero no puede usar la app (cliente o sin sector)
     */
    void ingresar(String usuario, char[] clave);
}