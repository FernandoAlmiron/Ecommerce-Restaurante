package org.example.escritorio.vista.comun;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.CannotCreateTransactionException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Window;
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
 */
public final class Tarea {

    private Tarea() {
    }

    public static <T> void ejecutar(Component origen, Callable<T> trabajo, Consumer<T> alTerminar) {
        ejecutar(origen, trabajo, alTerminar, mensaje -> Dialogos.error(origen, mensaje));
    }

    public static <T> void ejecutar(Component origen, Callable<T> trabajo, Consumer<T> alTerminar,
                                    Consumer<String> alFallar) {
        Window ventana = ventanaDe(origen);
        if (origen != null && !(origen instanceof Window)) {
            origen.setEnabled(false);
        }
        if (ventana != null) {
            ventana.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return trabajo.call();
            }

            @Override
            protected void done() {
                if (origen != null && !(origen instanceof Window)) {
                    origen.setEnabled(true);
                }
                if (ventana != null) {
                    ventana.setCursor(Cursor.getDefaultCursor());
                }

                T resultado;
                try {
                    resultado = get();
                } catch (CancellationException e) {
                    return;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (ExecutionException e) {
                    alFallar.accept(mensajePara(e.getCause()));
                    return;
                }
                alTerminar.accept(resultado);
            }
        }.execute();
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