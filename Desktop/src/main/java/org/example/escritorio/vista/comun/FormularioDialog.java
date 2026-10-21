package org.example.escritorio.vista.comun;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Window;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Ventana modal con un FormularioSimple y los botones Cancelar / Guardar, con la estética de DialogoFormulario.
 *
 * Al tocar Guardar llama a alGuardar (en el hilo de Swing): ahí el controlador lee los campos,
 * valida y devuelve el trabajo que llama al service. Si la validación falla lanza
 * IllegalArgumentException con el mensaje. El trabajo corre con Tarea; si el service rechaza los datos
 * se muestra el error y la ventana sigue abierta para corregir. Si sale bien, se cierra y devuelve lo guardado.
 */
public final class FormularioDialog<R> extends DialogoFormulario {

    private final FormularioSimple formulario;
    private R guardado;

    private FormularioDialog(Window padre, String seccion, String tituloVentana, FormularioSimple formulario,
                             Supplier<Callable<R>> alGuardar) {
        // el título del formulario (por ejemplo el nombre de la zona) va abajo, si no repite el de la ventana
        super(padre, seccion, tituloVentana,
                formulario.getTitulo().equals(tituloVentana) ? null : formulario.getTitulo(), "Guardar");
        this.formulario = formulario;
        btnPrincipal.addActionListener(e -> guardar(alGuardar));
        armar();
    }

    @Override
    protected JComponent armarCuerpo() {
        return formulario;
    }

    private void guardar(Supplier<Callable<R>> alGuardar) {
        Callable<R> trabajo;
        try {
            trabajo = alGuardar.get();
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
            return;
        }
        if (trabajo == null) {
            return; // el controlador decidió no seguir (por ejemplo, el usuario no confirmó)
        }
        Tarea.ejecutar(btnPrincipal, trabajo, resultado -> {
            guardado = resultado;
            dispose();
        });
    }

    /** Abre la ventana y espera. Devuelve lo que guardó el service, o vacío si se canceló. */
    public static <R> Optional<R> abrir(Component padre, String tituloVentana, FormularioSimple formulario,
                                        Supplier<Callable<R>> alGuardar) {
        return abrir(padre, "Administración", tituloVentana, formulario, alGuardar);
    }

    /** Igual que el otro, indicando la sección que va arriba del título (por ejemplo "Administración · Zonas"). */
    public static <R> Optional<R> abrir(Component padre, String seccion, String tituloVentana,
                                        FormularioSimple formulario, Supplier<Callable<R>> alGuardar) {
        Window ventana = padre instanceof Window w ? w : SwingUtilities.getWindowAncestor(padre);
        FormularioDialog<R> dialogo = new FormularioDialog<>(ventana, seccion, tituloVentana, formulario, alGuardar);
        dialogo.setVisible(true);
        return Optional.ofNullable(dialogo.guardado);
    }
}
