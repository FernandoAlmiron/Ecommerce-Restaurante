package org.example.escritorio.vista.cocina;

import org.example.Modelo.menu.Menu;
import org.example.escritorio.controlador.cocina.MenuEdicionControlador;
import org.example.escritorio.vista.comun.Dialogos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

/**
 * Alta o modificación de un plato (RF-26). Con plato null es un alta.
 * Que el nombre no esté vacío y el precio sea mayor a 0 lo valida MenuService.
 */
public class MenuDialog extends JDialog {

    private final Menu plato;

    private final JTextField txtNombre = new JTextField(28);
    private final JTextField txtDescripcion = new JTextField(28);
    private final JSpinner spnPrecio = new JSpinner(new SpinnerNumberModel(0d, 0d, 10_000_000d, 100d));
    private final JCheckBox chkDisponible = new JCheckBox("Disponible para pedir", true);
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnCancelar = new JButton("Cancelar");

    private final MenuEdicionControlador controlador;
    private boolean guardado;

    public MenuDialog(Window padre, Menu plato) {
        super(padre, plato == null ? "Nuevo plato" : "Modificar plato", ModalityType.APPLICATION_MODAL);
        this.plato = plato;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        spnPrecio.setEditor(new JSpinner.NumberEditor(spnPrecio, "#,##0.00"));

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        raiz.add(armarFormulario(), BorderLayout.CENTER);
        raiz.add(armarBotones(), BorderLayout.SOUTH);
        setContentPane(raiz);

        if (plato != null) {
            txtNombre.setText(plato.getNombre());
            txtDescripcion.setText(plato.getDescripcion() == null ? "" : plato.getDescripcion());
            spnPrecio.setValue(plato.getPrecio() == null ? 0d : plato.getPrecio().doubleValue());
            chkDisponible.setSelected(plato.isDisponible());
        }

        controlador = new MenuEdicionControlador(this);
        btnGuardar.addActionListener(e -> controlador.guardar());
        btnCancelar.addActionListener(e -> dispose());

        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    private JPanel armarFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 12);

        JLabel titulo = new JLabel(plato == null ? "Nuevo plato" : plato.getNombre());
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(titulo, c);
        c.gridwidth = 1;

        fila(panel, c, 1, "Nombre", txtNombre);
        fila(panel, c, 2, "Descripción", txtDescripcion);
        fila(panel, c, 3, "Precio ($)", spnPrecio);
        c.gridx = 1; c.gridy = 4;
        panel.add(chkDisponible, c);
        return panel;
    }

    private void fila(JPanel panel, GridBagConstraints c, int y, String etiqueta, JComponent campo) {
        c.gridx = 0; c.gridy = y; c.weightx = 0;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1; c.weightx = 1;
        panel.add(campo, c);
    }

    private JPanel armarBotones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnCancelar);
        botones.add(btnGuardar);
        getRootPane().setDefaultButton(btnGuardar);
        return botones;
    }

    // ---------- lo que usa el controlador ----------

    /** null si es un alta. */
    public Menu getPlato() { return plato; }
    public String getNombre() { return txtNombre.getText().trim(); }
    public String getDescripcion() { return txtDescripcion.getText().trim(); }
    public boolean isDisponible() { return chkDisponible.isSelected(); }

    /** El precio como BigDecimal con 2 decimales; null si lo escrito no es un número. */
    public BigDecimal getPrecio() {
        try {
            spnPrecio.commitEdit(); // toma lo escrito aunque no se haya salido del campo
        } catch (ParseException e) {
            return null;
        }
        return BigDecimal.valueOf(((Number) spnPrecio.getValue()).doubleValue()).setScale(2, RoundingMode.HALF_UP);
    }

    public JButton getBotonGuardar() { return btnGuardar; }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public void cerrarGuardado() {
        guardado = true;
        dispose();
    }

    public boolean seGuardo() {
        return guardado;
    }
}