package org.example.escritorio;

import org.example.escritorio.sesion.LoginDePrueba;
import org.example.escritorio.sesion.ServicioLogin;
import org.example.escritorio.vista.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Arranca la app de escritorio.
 * Cuando se separe el backend, esta clase se reemplaza por AppEscritorio (que arranca Spring).
 */
public class Main {

    public static void main(String[] args) {
        aplicarAspecto();
        registrarServicios();
        // toda la interfaz se crea en el hilo de Swing
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    /** Por ahora, servicios de prueba. Después los va a dar Spring y este método desaparece. */
    private static void registrarServicios() {
        Contexto.registrar(ServicioLogin.class, new LoginDePrueba());
    }

    /** Usa FlatLaf si está en el classpath; si no, el aspecto del sistema operativo. */
    private static void aplicarAspecto() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception sinFlatLaf) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorado) {
                // queda el aspecto por defecto de Swing
            }
        }
    }
}