package org.example.escritorio.vista.caja;

import org.example.Modelo.reserva.Tarjeta;
import org.example.escritorio.controlador.caja.TarjetaEdicionControlador;
import org.example.escritorio.vista.comun.Dialogos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.YearMonth;
import java.util.Optional;

/** Alta de una tarjeta (RF-46): titular como figura en la tarjeta y mes/año de vencimiento. */
public class TarjetaDialog extends JDialog {

    private final JTextField txtTitular = new JTextField(24);
    private final JComboBox<String> cmbMes = new JComboBox<>(new String[]{
            "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"});
    private final JSpinner spnAnio;
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnCancelar = new JButton("Cancelar");

    private final TarjetaEdicionControlador controlador;
    private Tarjeta guardada;

    public TarjetaDialog(Window padre) {
        super(padre, "Nueva tarjeta", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        int anioActual = YearMonth.now().getYear();
        spnAnio = new JSpinner(new SpinnerNumberModel(anioActual, anioActual, anioActual + 20, 1));
        spnAnio.setEditor(new JSpinner.NumberEditor(spnAnio, "0"));
        cmbMes.setSelectedIndex(YearMonth.now().getMonthValue() - 1);

        JPanel vencimiento = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        vencimiento.add(cmbMes);
        vencimiento.add(new JLabel("/"));
        vencimiento.add(spnAnio);

        JPanel formulario = new JPanel(new GridLayout(0, 2, 12, 8));
        formulario.add(new JLabel("Titular (como figura en la tarjeta)"));
        formulario.add(txtTitular);
        formulario.add(new JLabel("Vencimiento (mes / año)"));
        formulario.add(vencimiento);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnCancelar);
        botones.add(btnGuardar);
        getRootPane().setDefaultButton(btnGuardar);

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        raiz.add(formulario, BorderLayout.CENTER);
        raiz.add(botones, BorderLayout.SOUTH);
        setContentPane(raiz);

        controlador = new TarjetaEdicionControlador(this);
        btnGuardar.addActionListener(e -> controlador.guardar());
        btnCancelar.addActionListener(e -> dispose());

        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    // ---------- lo que usa el controlador ----------

    /** En mayúsculas, como se imprime en las tarjetas. */
    public String getTitular() { return txtTitular.getText().trim().toUpperCase(); }

    public YearMonth getVencimiento() {
        return YearMonth.of((Integer) spnAnio.getValue(), cmbMes.getSelectedIndex() + 1);
    }

    public JButton getBotonGuardar() { return btnGuardar; }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public void cerrarCon(Tarjeta tarjeta) {
        guardada = tarjeta;
        dispose();
    }

    public Optional<Tarjeta> getGuardada() {
        return Optional.ofNullable(guardada);
    }
}