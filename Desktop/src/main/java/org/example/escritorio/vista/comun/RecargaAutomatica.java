package org.example.escritorio.vista.comun;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.HierarchyEvent;

/**
 * Hace que una pantalla se recargue sola cada 10 segundos, para que no se pase un pedido, una reserva o un
 * cobro aunque nadie toque "Actualizar". También se recarga al volver a la solapa.
 *
 * La recarga es "en silencio" (ver Tarea): no ocupa botones, no muestra errores y deja la fila elegida y el
 * scroll donde estaban. Solo corre si la pantalla está a la vista y no hay un diálogo abierto encima
 * (por ejemplo, cargando un pedido o editando un proveedor).
 *
 * Uso, al final del constructor de la pantalla:
 *   RecargaAutomatica.instalar(this, () -> controlador.cargar(null));
 */
public final class RecargaAutomatica {

    public static final int CADA_MS = 10_000;

    private RecargaAutomatica() {
    }

    public static void instalar(JComponent pantalla, Runnable recargar) {
        Tarea.Recarga recarga = new Tarea.Recarga(pantalla);
        Runnable siSePuede = () -> {
            if (recarga.enCurso == 0 && sePuedeRecargar(pantalla)) {
                Tarea.enSilencio(recarga, recargar);
            }
        };
        Timer timer = new Timer(CADA_MS, e -> siSePuede.run());

        // el timer vive mientras la pantalla está en una ventana; al cerrar sesión se apaga
        pantalla.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
                if (pantalla.isDisplayable()) {
                    timer.start();
                } else {
                    timer.stop();
                }
            }
            // al volver a la solapa se ve lo último, sin esperar los 10 segundos
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && pantalla.isShowing()) {
                timer.restart();
                SwingUtilities.invokeLater(siSePuede);
            }
        });
        if (pantalla.isDisplayable()) {
            timer.start();
        }
    }

    private static boolean sePuedeRecargar(JComponent pantalla) {
        if (!pantalla.isShowing()) {
            return false;
        }
        Window ventana = SwingUtilities.getWindowAncestor(pantalla);
        return ventana != null && !hayDialogoAbierto(ventana);
    }

    /** Un diálogo modal abierto desde la ventana (o desde otro diálogo de ella): lo de atrás no se toca. */
    private static boolean hayDialogoAbierto(Window ventana) {
        for (Window hija : ventana.getOwnedWindows()) {
            if (hija.isShowing() && (hija instanceof Dialog d && d.isModal() || hayDialogoAbierto(hija))) {
                return true;
            }
        }
        return false;
    }
}
