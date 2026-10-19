package org.example.escritorio.controlador;

import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.AutenticacionService;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.LoginFrame;
import org.example.escritorio.vista.comun.Tarea;

import java.util.Arrays;

/**
 * Lógica de la pantalla de login: valida que haya datos, verifica usuario y clave con el backend
 * y según el resultado abre la ventana principal o muestra el error.
 */
public class LoginControlador {

    private final LoginFrame vista;
    private final AutenticacionService autenticacion;

    public LoginControlador(LoginFrame vista) {
        this.vista = vista;
        this.autenticacion = Contexto.get(AutenticacionService.class);
    }

    public void ingresar(String usuario, char[] clave) {
        String usuarioLimpio = usuario == null ? "" : usuario.trim();
        if (usuarioLimpio.isEmpty() || clave.length == 0) {
            Arrays.fill(clave, '\0');
            vista.mostrarError("Ingresá usuario y contraseña.");
            return;
        }

        Tarea.ejecutar(vista.getBotonIngresar(),
                () -> {
                    try {
                        autenticacion.ingresar(usuarioLimpio, clave);
                        return null;
                    } finally {
                        Arrays.fill(clave, '\0'); // la clave no queda en memoria
                    }
                },
                sinResultado -> vista.irAPantallaPrincipal(),
                mensaje -> {
                    Sesion.cerrar();
                    // primero el mensaje (es modal) y después se limpia la clave, así el cursor queda en ese campo
                    vista.mostrarError(mensaje);
                    vista.limpiarClave();
                });
    }
}