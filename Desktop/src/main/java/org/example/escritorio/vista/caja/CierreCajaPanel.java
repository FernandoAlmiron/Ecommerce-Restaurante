package org.example.escritorio.vista.caja;

import org.example.Modelo.facturacion.CierreCaja;
import org.example.escritorio.controlador.caja.CierreCajaControlador;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Formatos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.List;

/**
 * Cierre de caja del día (RF-47).
 * Debajo del efectivo se ve lo cobrado hoy en salón con tarjeta y QR, que no hay que contar.
 * El efectivo se cuenta a mano: el backend calcula lo esperado y la diferencia, y lo esperado
 * no se muestra antes de contar. Después del cierre se ve el total del día: efectivo, tarjeta y QR.
 */
public class CierreCajaPanel extends JPanel {

    private static final Color ROJO = new Color(0xC62828);
    private static final Color VERDE = new Color(0x2E7D32);

    private final JSpinner spnContado = new JSpinner(new SpinnerNumberModel(0d, 0d, 100_000_000d, 1000d));
    private final JTextField txtObservaciones = new JTextField(30);
    private final JButton btnCerrar = new JButton("Cerrar caja");
    private final JLabel lblTarjeta = new JLabel("—");
    private final JLabel lblQr = new JLabel("—");
    private final JLabel lblArqueo = new JLabel(" ");
    private final JLabel lblTotalDia = new JLabel(" ");

    private final CierreTableModel modelo = new CierreTableModel();
    private final JTable tabla = new JTable(modelo);
    private final JButton btnActualizar = new JButton("Actualizar");

    private final CierreCajaControlador controlador;

    public CierreCajaPanel() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        spnContado.setEditor(new JSpinner.NumberEditor(spnContado, "#,##0.00"));
        ((JSpinner.DefaultEditor) spnContado.getEditor()).getTextField().setColumns(12);

        add(armarFormulario(), BorderLayout.NORTH);
        add(armarHistorial(), BorderLayout.CENTER);

        controlador = new CierreCajaControlador(this);
        btnCerrar.addActionListener(e -> controlador.cerrarCaja());
        btnActualizar.addActionListener(e -> controlador.cargar());

        controlador.cargar();
    }

    private JPanel armarFormulario() {
        JLabel titulo = new JLabel("Cierre de caja de hoy");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 15f));
        JLabel ayuda = new JLabel("Contá el efectivo de la caja y cargalo. Tarjeta y QR los suma el sistema. "
                + "El delivery tiene su propio reporte.");

        lblTarjeta.setFont(lblTarjeta.getFont().deriveFont(Font.BOLD));
        lblQr.setFont(lblQr.getFont().deriveFont(Font.BOLD));

        JPanel campos = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 0, 4, 12);
        fila(campos, c, 0, "Efectivo contado $", spnContado);
        fila(campos, c, 1, "Tarjeta (cobrado hoy)", lblTarjeta);
        fila(campos, c, 2, "QR (cobrado hoy)", lblQr);
        fila(campos, c, 3, "Observaciones", txtObservaciones);
        c.gridx = 1; c.gridy = 4;
        c.insets = new Insets(8, 0, 4, 12);
        campos.add(btnCerrar, c);

        lblArqueo.setFont(lblArqueo.getFont().deriveFont(Font.BOLD, 14f));
        lblTotalDia.setFont(lblTotalDia.getFont().deriveFont(Font.BOLD, 14f));
        JPanel resultado = new JPanel(new GridLayout(0, 1, 0, 4));
        resultado.add(lblArqueo);
        resultado.add(lblTotalDia);

        JPanel textos = new JPanel(new GridLayout(0, 1, 0, 4));
        textos.add(titulo);
        textos.add(ayuda);

        JPanel formulario = new JPanel(new BorderLayout(0, 8));
        formulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        formulario.add(textos, BorderLayout.NORTH);
        JPanel alineado = new JPanel(new BorderLayout()); // los campos pegados a la izquierda
        alineado.add(campos, BorderLayout.WEST);
        formulario.add(alineado, BorderLayout.CENTER);
        formulario.add(resultado, BorderLayout.SOUTH);
        return formulario;
    }

    private void fila(JPanel panel, GridBagConstraints c, int y, String etiqueta, JComponent campo) {
        c.gridx = 0; c.gridy = y;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1;
        panel.add(campo, c);
    }

    private JPanel armarHistorial() {
        TableRowSorter<CierreTableModel> orden = new TableRowSorter<>(modelo);
        orden.setSortKeys(List.of(new RowSorter.SortKey(CierreTableModel.COL_NRO, SortOrder.DESCENDING)));
        tabla.setRowSorter(orden);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(26);
        tabla.setRowSelectionAllowed(false);
        Formatos.aplicarA(tabla);
        tabla.getColumnModel().getColumn(CierreTableModel.COL_DIFERENCIA).setCellRenderer(new RendererDiferencia());
        tabla.getColumnModel().getColumn(CierreTableModel.COL_TOTAL).setCellRenderer(new RendererTotal());
        int[] anchos = {40, 85, 130, 110, 110, 95, 100, 100, 110, 160};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JLabel titulo = new JLabel("Cierres anteriores");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD));
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(btnActualizar, BorderLayout.EAST);

        JPanel historial = new JPanel(new BorderLayout(0, 8));
        historial.add(encabezado, BorderLayout.NORTH);
        historial.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return historial;
    }

    // ---------- lo que usa el controlador ----------

    /** El monto contado con 2 decimales; null si lo escrito no es un número. */
    public BigDecimal getMontoContado() {
        try {
            spnContado.commitEdit();
        } catch (ParseException e) {
            return null;
        }
        return BigDecimal.valueOf(((Number) spnContado.getValue()).doubleValue()).setScale(2, RoundingMode.HALF_UP);
    }

    public String getObservaciones() { return txtObservaciones.getText().trim(); }

    public JButton getBotonCerrar() { return btnCerrar; }
    public JButton getBotonActualizar() { return btnActualizar; }

    public void mostrarCierres(List<CierreFila> cierres) {
        modelo.setFilas(cierres);
    }

    /** Lo cobrado hoy en salón con tarjeta y QR, antes de cerrar la caja. */
    public void mostrarCobradoHoy(BigDecimal tarjeta, BigDecimal qr) {
        lblTarjeta.setText(Formatos.pesos(tarjeta));
        lblQr.setText(Formatos.pesos(qr));
    }

    /** Muestra el resultado del cierre recién hecho y limpia el formulario. */
    public void mostrarResultado(CierreFila fila) {
        CierreCaja cierre = fila.cierre();
        String arqueo = "Efectivo: esperado " + Formatos.pesos(cierre.getMontoEsperado())
                + " · contado " + Formatos.pesos(cierre.getMontoContado()) + " · ";
        if (cierre.faltoPlata()) {
            lblArqueo.setForeground(ROJO);
            arqueo += "faltan " + Formatos.pesos(cierre.getDiferencia().negate());
        } else if (cierre.sobroPlata()) {
            lblArqueo.setForeground(ROJO);
            arqueo += "sobran " + Formatos.pesos(cierre.getDiferencia());
        } else {
            lblArqueo.setForeground(VERDE);
            arqueo += "la caja cierra justa";
        }
        lblArqueo.setText(arqueo);
        lblTotalDia.setText("Tarjeta " + Formatos.pesos(fila.tarjeta()) + " · QR " + Formatos.pesos(fila.qr())
                + " · Total del día " + Formatos.pesos(fila.totalDelDia()));

        spnContado.setValue(0d);
        txtObservaciones.setText("");
    }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public boolean confirmar(String pregunta) {
        return Dialogos.confirmar(this, pregunta);
    }

    /** Diferencia en rojo si falta o sobra plata, en verde si da justo. */
    private static class RendererDiferencia extends DefaultTableCellRenderer {
        RendererDiferencia() {
            setHorizontalAlignment(RIGHT);
        }

        @Override
        protected void setValue(Object valor) {
            BigDecimal diferencia = (BigDecimal) valor;
            setText(diferencia == null ? "—" : Formatos.pesos(diferencia));
            setForeground(diferencia == null || diferencia.signum() == 0 ? VERDE : ROJO);
        }
    }

    /** Total del día en negrita. */
    private static class RendererTotal extends DefaultTableCellRenderer {
        RendererTotal() {
            setHorizontalAlignment(RIGHT);
            setFont(getFont().deriveFont(Font.BOLD));
        }

        @Override
        protected void setValue(Object valor) {
            setText(valor == null ? "—" : Formatos.pesos((BigDecimal) valor));
            setFont(getFont().deriveFont(Font.BOLD));
        }
    }
}