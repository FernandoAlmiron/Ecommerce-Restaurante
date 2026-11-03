package org.example.escritorio.vista.comun;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.CannotCreateTransactionException;

import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Ejecuta un trabajo lento (consultar la base, llamar a un service) en segundo plano,
 * para que la ventana no se congele.
 *
 * Uso típico desde un controlador:
 *   Tarea.ejecutar(botonConfirmar,
 *           () -> reservaService.confirmar(id),   // corre en segundo plano
 *           reserva -> vista.actualizar(reserva)); // corre en el hilo de Swing
 *
 * Mientras corre, el componente que lo disparó queda deshabilitado y el cursor en espera.
 * Si falla, por defecto se muestra el mensaje del error en un diálogo.
 *
 * Las tareas que arranca la recarga automática (ver RecargaAutomatica) corren "en silencio": no ocupan el
 * botón ni ponen el cursor en espera, un error no abre diálogo y el scroll de la pantalla queda donde estaba.
 */
public final class Tarea {

    private Tarea() {
    }

    /** Una pantalla que se recarga sola, y cuántas de sus tareas siguen corriendo (para no encimarlas). */
    static final class Recarga {
        final Container pantalla;
        int enCurso;

        Recarga(Container pantalla) {
            this.pantalla = pantalla;
        }
    }

    /** La recarga automática que está arrancando tareas en este momento, o null. Solo se usa en el hilo de Swing. */
    private static Recarga recargaActual;

    /** Corre el trabajo (que arranca tareas) como parte de esa recarga automática. */
    static void enSilencio(Recarga recarga, Runnable trabajo) {
        Recarga antes = recargaActual;
        recargaActual = recarga;
        try {
            trabajo.run();
        } finally {
            recargaActual = antes;
        }
    }

    public static <T> void ejecutar(Component origen, Callable<T> trabajo, Consumer<T> alTerminar) {
        ejecutar(origen, trabajo, alTerminar, null);
    }

    /** @param alFallar qué hacer con el mensaje si falla; null para mostrarlo en un diálogo */
    public static <T> void ejecutar(Component origen, Callable<T> trabajo, Consumer<T> alTerminar,
                                    Consumer<String> alFallar) {
        Recarga recarga = recargaActual;
        Consumer<String> siFalla;
        if (alFallar != null) {
            siFalla = alFallar;
        } else if (recarga != null) {
            siFalla = mensaje -> { };   // en la recarga automática no se interrumpe: se reintenta en la próxima
        } else {
            siFalla = mensaje -> Dialogos.error(origen, mensaje);
        }
        // en la recarga automática, ni botón ocupado ni cursor en espera
        Component boton = recarga == null ? origen : null;
        Window ventana = ventanaDe(boton);
        if (boton != null && !(boton instanceof Window)) {
            boton.setEnabled(false);
        }
        if (ventana != null) {
            ventana.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        if (recarga != null) {
            recarga.enCurso++;
        }

        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return trabajo.call();
            }

            @Override
            protected void done() {
                if (boton != null && !(boton instanceof Window)) {
                    boton.setEnabled(true);
                }
                if (ventana != null) {
                    ventana.setCursor(Cursor.getDefaultCursor());
                }
                if (recarga == null) {
                    terminar();
                    return;
                }
                // lo que se arranque al terminar (por ejemplo, los pedidos de la fila elegida) también es parte
                // de la recarga; y la pantalla no tiene que saltar hasta la fila elegida
                recarga.enCurso--;
                Map<JViewport, Point> scroll = posicionesDelScroll(recarga.pantalla);
                enSilencio(recarga, this::terminar);
                scroll.forEach(Tarea::volverA);
                SwingUtilities.invokeLater(() -> scroll.forEach(Tarea::volverA));   // y después de reacomodarse
            }

            private void terminar() {
                T resultado;
                try {
                    resultado = get();
                } catch (CancellationException e) {
                    return;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (ExecutionException e) {
                    siFalla.accept(mensajePara(e.getCause()));
                    return;
                }
                alTerminar.accept(resultado);
            }
        }.execute();
    }

    /** Dónde está parado cada scroll de la pantalla. */
    private static Map<JViewport, Point> posicionesDelScroll(Container contenedor) {
        Map<JViewport, Point> posiciones = new HashMap<>();
        for (Component c : contenedor.getComponents()) {
            if (c instanceof JViewport v && v.getView() != null) {
                posiciones.put(v, v.getViewPosition());
            }
            if (c instanceof Container hijo) {
                posiciones.putAll(posicionesDelScroll(hijo));
            }
        }
        return posiciones;
    }

    /** Vuelve el scroll adonde estaba, sin pasarse si ahora hay menos filas. */
    private static void volverA(JViewport viewport, Point antes) {
        Component vista = viewport.getView();
        if (vista == null) {
            return;
        }
        Dimension tamanio = vista.getPreferredSize();
        Dimension visible = viewport.getExtentSize();
        Point nueva = new Point(Math.max(0, Math.min(antes.x, tamanio.width - visible.width)),
                Math.max(0, Math.min(antes.y, tamanio.height - visible.height)));
        if (!nueva.equals(viewport.getViewPosition())) {
            viewport.setViewPosition(nueva);
        }
    }

    /**
     * Traduce lo que lanzan los services del backend a un mensaje para el usuario,
     * con el mismo criterio que el ManejadorErrores de la API:
     * IllegalArgumentException (dato inválido), IllegalStateException (regla de negocio) y
     * NoSuchElementException (no existe) traen un mensaje ya pensado para mostrar.
     */
    private static String mensajePara(Throwable error) {
        if (error instanceof NoSuchElementException) {
            // orElseThrow() sin mensaje trae "No value present": no le sirve a nadie
            return error.getMessage() == null || error.getMessage().equals("No value present")
                    ? "No se encontró el registro pedido."
                    : error.getMessage();
        }
        if ((error instanceof IllegalArgumentException || error instanceof IllegalStateException)
                && error.getMessage() != null) {
            return error.getMessage();
        }
        if (error instanceof DataIntegrityViolationException) {
            // el mensaje real de MySQL muestra tablas y columnas: no se muestra
            return "Ya existe un registro con esos datos, o falta un dato obligatorio.";
        }
        if (error instanceof DataAccessResourceFailureException || error instanceof CannotCreateTransactionException) {
            error.printStackTrace();
            return "No se puede conectar con la base de datos. Verificá que MySQL esté encendido.";
        }
        // no es un error esperable: es un bug, el detalle queda en consola
        error.printStackTrace();
        return "Ocurrió un error inesperado en la aplicación.";
    }

    private static Window ventanaDe(Component origen) {
        if (origen == null) {
            return null;
        }
        return origen instanceof Window w ? w : SwingUtilities.getWindowAncestor(origen);
    }
}