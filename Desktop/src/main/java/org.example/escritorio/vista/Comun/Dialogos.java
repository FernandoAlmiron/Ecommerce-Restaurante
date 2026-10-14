package org.example.escritorio.vista.comun;

import javax.swing.JOptionPane;
import java.awt.Component;

/** Todos los mensajes de la app pasan por acá, así se ven iguales. */
public final class Dialogos {

    private Dialogos() {
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Devuelve true si el usuario eligió "Sí". */
    public static boolean confirmar(Component padre, String mensaje) {
        int opcion = JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return opcion == JOptionPane.YES_OPTION;
    }
}