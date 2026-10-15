package org.example.escritorio.vista.caja;

import org.example.Modelo.reserva.Tarjeta;
import org.example.escritorio.controlador.caja.TarjetasControlador;
import org.example.escritorio.vista.comun.Dialogos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Tarjetas registradas (RF-46): titular y vencimiento. Son las que se usan para la seña de las reservas.
 * El número de la tarjeta no se guarda.
 */
public class TarjetasPanel extends JPanel {

    private final TarjetaTableModel modelo = new TarjetaTableModel();
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<TarjetaTableModel> orden = new TableRowSorter<>(modelo);

    private final JTextField txtBuscar = new JTextField(16);
    private final JButton btnActualizar = new JButton("Actualizar");
    private final JButton btnNueva = new JButton("Nueva tarjeta");
    private final JLabel lblResumen = new JLabel(" ");

    private final TarjetasControlador controlador;

    public TarjetasPanel() {
        super(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izquierda.add(new JLabel("Buscar titular:"));
        izquierda.add(txtBuscar);
        izquierda.add(btnActualizar);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(btnNueva);
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.add(izquierda, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);

        orden.setSortKeys(List.of(new RowSorter.SortKey(TarjetaTableModel.COL_NRO, SortOrder.DESCENDING)));
        tabla.setRowSorter(orden);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(26);
        tabla.setRowSelectionAllowed(false);
        int[] anchos = {50, 260, 110, 70};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        add(encabezado, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(lblResumen, BorderLayout.SOUTH);

        controlador = new TarjetasControlador(this);
        btnActualizar.addActionListener(e -> controlador.cargar());
        btnNueva.addActionListener(e -> controlador.nueva());
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { aplicarFiltro(); }
            @Override public void removeUpdate(DocumentEvent e) { aplicarFiltro(); }
            @Override public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
        });

        controlador.cargar();
    }

    // ---------- lo que usa el controlador ----------

    public void mostrarTarjetas(List<Tarjeta> tarjetas) {
        modelo.setFilas(tarjetas);
        aplicarFiltro();
    }

    public JButton getBotonActualizar() { return btnActualizar; }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public void mostrarInfo(String mensaje) {
        Dialogos.info(this, mensaje);
    }

    private void aplicarFiltro() {
        String texto = txtBuscar.getText().trim().toLowerCase();
        orden.setRowFilter(texto.isEmpty() ? null : new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends TarjetaTableModel, ? extends Integer> fila) {
                String titular = fila.getModel().getFila(fila.getIdentifier()).getTitular();
                return titular != null && titular.toLowerCase().contains(texto);
            }
        });
        lblResumen.setText(tabla.getRowCount() + " de " + modelo.getRowCount() + " tarjetas");
    }
}