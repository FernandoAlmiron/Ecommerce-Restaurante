package org.example.escritorio.vista.comun;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.OptionalDouble;

/**
 * Ventana chica para pedir una cantidad con decimales (kilos, litros, unidades).
 * El mínimo que se pasa es del formulario (por ejemplo, no reponer 0); las reglas de negocio las valida el backend.
 *
 *   OptionalDouble cantidad = CantidadDialog.pedir(panel, "Reponer", "Cantidad recibida", "kg", 1, 0.001);
 */
public final class CantidadDialog extends JDialog {

    private final JSpinner spnCantidad;
    private final double minimo;
    private Double resultado;

    private CantidadDialog(Window padre, String titulo, String etiqueta, String unidad,
                           double inicial, double minimo) {
        super(padre, titulo, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.minimo = minimo;

        spnCantidad = new JSpinner(new SpinnerNumberModel(Math.max(inicial, minimo), minimo, 1_000_000d, 1d));
        spnCantidad.setEditor(new JSpinner.NumberEditor(spnCantidad, "0.###"));
        ((JSpinner.DefaultEditor) spnCantidad.getEditor()).getTextField().setColumns(8);

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fila.add(new JLabel(etiqueta));
        fila.add(spnCantidad);
        fila.add(new JLabel(unidad == null ? "" : unidad));

        JButton btnAceptar = new JButton("Aceptar");
        JButton btnCancelar = new JButton("Cancelar");
        btnAceptar.addActionListener(e -> aceptar());
        btnCancelar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnCancelar);
        botones.add(btnAceptar);
        getRootPane().setDefaultButton(btnAceptar);

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        raiz.add(fila, BorderLayout.CENTER);
        raiz.add(botones, BorderLayout.SOUTH);
        setContentPane(raiz);

        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    /** Abre la ventana y devuelve la cantidad, o vacío si se canceló. */
    public static OptionalDouble pedir(Component padre, String titulo, String etiqueta, String unidad,
                                       double inicial, double minimo) {
        Window ventana = padre instanceof Window w ? w : SwingUtilities.getWindowAncestor(padre);
        CantidadDialog dialogo = new CantidadDialog(ventana, titulo, etiqueta, unidad, inicial, minimo);
        dialogo.setVisible(true);
        return dialogo.resultado == null ? OptionalDouble.empty() : OptionalDouble.of(dialogo.resultado);
    }

    private void aceptar() {
        try {
            spnCantidad.commitEdit(); // toma lo que se escribió aunque no se haya salido del campo
        } catch (ParseException e) {
            Dialogos.error(this, "Escribí un número válido, desde " + new DecimalFormat("0.###").format(minimo) + ".");
            return;
        }
        resultado = ((Number) spnCantidad.getValue()).doubleValue();
        dispose();
    }
}