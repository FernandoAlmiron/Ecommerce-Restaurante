package org.example.escritorio.vista.comun;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Ventana modal con un FormularioSimple y los botones Guardar / Cancelar.
 *
 * Al tocar Guardar llama a alGuardar (en el hilo de Swing): ahí el controlador lee los campos,
 * valida y devuelve el trabajo que llama al service. Si la validación falla lanza
 * IllegalArgumentException con el mensaje. El trabajo corre con Tarea; si el service rechaza los datos
 * se muestra el error y la ventana sigue abierta para corregir. Si sale bien, se cierra y devuelve lo guardado.
 */
public final class FormularioDialog<R> extends JDialog {

    private final JButton btnGuardar = new JButton("Guardar");
    private R guardado;

    private FormularioDialog(Window padre, FormularioSimple formulario, Supplier<Callable<R>> alGuardar) {
        super(padre, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar(alGuardar));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnCancelar);
        botones.add(btnGuardar);
        getRootPane().setDefaultButton(btnGuardar);

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        raiz.add(formulario, BorderLayout.CENTER);
        raiz.add(botones, BorderLayout.SOUTH);
        setContentPane(raiz);

        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    private void guardar(Supplier<Callable<R>> alGuardar) {
        Callable<R> trabajo;
        try {
            trabajo = alGuardar.get();
        } catch (IllegalArgumentException e) {
            Dialogos.error(this, e.getMessage());
            return;
        }
        if (trabajo == null) {
            return; // el controlador decidió no seguir (por ejemplo, el usuario no confirmó)
        }
        Tarea.ejecutar(btnGuardar, trabajo, resultado -> {
            guardado = resultado;
            dispose();
        });
    }

    /** Abre la ventana y espera. Devuelve lo que guardó el service, o vacío si se canceló. */
    public static <R> Optional<R> abrir(Component padre, String tituloVentana, FormularioSimple formulario,
                                        Supplier<Callable<R>> alGuardar) {
        Window ventana = padre instanceof Window w ? w : SwingUtilities.getWindowAncestor(padre);
        FormularioDialog<R> dialogo = new FormularioDialog<>(ventana, formulario, alGuardar);
        dialogo.setTitle(tituloVentana);
        dialogo.setVisible(true);
        return Optional.ofNullable(dialogo.guardado);
    }
}