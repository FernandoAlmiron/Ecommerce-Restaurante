package org.example.escritorio.vista.comun;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
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
public final class CantidadDialog extends DialogoFormulario {

    private final String etiqueta;
    private final String unidad;
    private final JSpinner spnCantidad;
    private final double minimo;
    private Double resultado;

    private CantidadDialog(Window padre, String titulo, String etiqueta, String unidad,
                           double inicial, double minimo) {
        super(padre, "", titulo, null, "Aceptar");
        this.etiqueta = etiqueta;
        this.unidad = unidad;
        this.minimo = minimo;

        spnCantidad = new JSpinner(new SpinnerNumberModel(Math.max(inicial, minimo), minimo, 1_000_000d, 1d));
        spnCantidad.setEditor(new JSpinner.NumberEditor(spnCantidad, "0.###"));

        btnPrincipal.addActionListener(e -> aceptar());

        armar();
    }

    @Override
    protected JComponent armarCuerpo() {
        Formulario.estilizar(spnCantidad);
        // la etiqueta viene como "Cantidad recibida:"; en el formulario va sin los dos puntos
        String texto = etiqueta.endsWith(":") ? etiqueta.substring(0, etiqueta.length() - 1) : etiqueta;

        JPanel cantidad = Estilo.transparente(new BorderLayout(12, 0));
        cantidad.add(spnCantidad, BorderLayout.CENTER);
        if (unidad != null && !unidad.isBlank()) {
            cantidad.add(Formulario.nota(new JLabel(unidad)), BorderLayout.EAST);
        }
        return Formulario.cuerpo(Formulario.campo(texto, cantidad, null));
    }

    @Override
    protected int anchoMinimo() {
        return 440;
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
            mostrarError("Escribí un número válido, desde " + new DecimalFormat("0.###").format(minimo) + ".");
            return;
        }
        resultado = ((Number) spnCantidad.getValue()).doubleValue();
        dispose();
    }
}
