package org.example.escritorio.vista.cocina;

import org.example.Modelo.inventario.Stock;
import org.example.escritorio.controlador.cocina.StockControlador;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.comun.Dialogos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Optional;

/**
 * Stock de insumos (RF-30, RF-31): alta, modificación, reponer y ajustar.
 * Los insumos bajo mínimo (con la alerta activa) se ven en rojo; eliminar es solo para ADMIN (RF-32).
 */
public class StockPanel extends JPanel {

    private static final Color ROJO = new Color(0xC62828);

    private final StockTableModel modelo = new StockTableModel();
    private final JTable tabla = new JTable(modelo) {
        /** Pinta en rojo la fila completa de los insumos bajo mínimo. */
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int fila, int columna) {
            Component celda = super.prepareRenderer(renderer, fila, columna);
            if (!isRowSelected(fila)) {
                boolean bajo = modelo.getFila(convertRowIndexToModel(fila)).alcanzoStockMinimo();
                celda.setForeground(bajo ? ROJO : getForeground());
                celda.setFont(bajo ? celda.getFont().deriveFont(Font.BOLD) : celda.getFont());
            }
            return celda;
        }
    };
    private final TableRowSorter<StockTableModel> orden = new TableRowSorter<>(modelo);

    private final JCheckBox chkSoloBajoMinimo = new JCheckBox("Solo bajo mínimo");
    private final JButton btnActualizar = new JButton("Actualizar");
    private final JButton btnNuevo = new JButton("Nuevo insumo");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnReponer = new JButton("Reponer");
    private final JButton btnAjustar = new JButton("Ajustar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JLabel lblResumen = new JLabel(" ");

    private final StockControlador controlador;

    public StockPanel() {
        super(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(armarEncabezado(), BorderLayout.NORTH);
        add(armarTabla(), BorderLayout.CENTER);
        add(armarAcciones(), BorderLayout.SOUTH);

        controlador = new StockControlador(this);

        chkSoloBajoMinimo.addActionListener(e -> aplicarFiltro());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                habilitarAcciones(insumoSeleccionado().isPresent());
            }
        });
        btnActualizar.addActionListener(e -> controlador.cargar());
        btnNuevo.addActionListener(e -> controlador.nuevo());
        btnModificar.addActionListener(e -> controlador.modificar());
        btnReponer.addActionListener(e -> controlador.reponer());
        btnAjustar.addActionListener(e -> controlador.ajustar());
        btnEliminar.addActionListener(e -> controlador.eliminar());

        habilitarAcciones(false);
        controlador.cargar();
    }

    private JPanel armarEncabezado() {
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izquierda.add(chkSoloBajoMinimo);
        izquierda.add(btnActualizar);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(btnNuevo);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.add(izquierda, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);
        return encabezado;
    }

    private JScrollPane armarTabla() {
        orden.setSortKeys(List.of(new RowSorter.SortKey(StockTableModel.COL_INSUMO, SortOrder.ASCENDING)));
        tabla.setRowSorter(orden);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(26);
        int[] anchos = {220, 160, 80, 100, 100, 70};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        return new JScrollPane(tabla);
    }

    private JPanel armarAcciones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        if (Sesion.esAdmin()) {
            botones.add(btnEliminar);
        }
        botones.add(btnModificar);
        botones.add(btnAjustar);
        botones.add(btnReponer);

        JPanel acciones = new JPanel(new BorderLayout());
        acciones.add(lblResumen, BorderLayout.WEST);
        acciones.add(botones, BorderLayout.EAST);
        return acciones;
    }

    // ---------- lo que usa el controlador ----------

    public void mostrarInsumos(List<Stock> insumos) {
        modelo.setFilas(insumos);
        aplicarFiltro();
    }

    public Optional<Stock> insumoSeleccionado() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return Optional.empty();
        }
        return Optional.of(modelo.getFila(tabla.convertRowIndexToModel(filaVista)));
    }

    /** Deja seleccionado el insumo que se acaba de tocar (después de recargar). */
    public void seleccionarInsumo(int idStock) {
        for (int fila = 0; fila < tabla.getRowCount(); fila++) {
            if (modelo.getFila(tabla.convertRowIndexToModel(fila)).getIdStock() == idStock) {
                tabla.setRowSelectionInterval(fila, fila);
                tabla.scrollRectToVisible(tabla.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    public void habilitarAcciones(boolean hayInsumo) {
        btnModificar.setEnabled(hayInsumo);
        btnReponer.setEnabled(hayInsumo);
        btnAjustar.setEnabled(hayInsumo);
        btnEliminar.setEnabled(hayInsumo);
    }

    public JButton getBotonActualizar() { return btnActualizar; }
    public JButton getBotonReponer() { return btnReponer; }
    public JButton getBotonAjustar() { return btnAjustar; }
    public JButton getBotonEliminar() { return btnEliminar; }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public boolean confirmar(String pregunta) {
        return Dialogos.confirmar(this, pregunta);
    }

    // ---------- filtro ----------

    private void aplicarFiltro() {
        orden.setRowFilter(chkSoloBajoMinimo.isSelected() ? new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends StockTableModel, ? extends Integer> fila) {
                return fila.getModel().getFila(fila.getIdentifier()).alcanzoStockMinimo();
            }
        } : null);

        int bajoMinimo = 0;
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (modelo.getFila(i).alcanzoStockMinimo()) {
                bajoMinimo++;
            }
        }
        lblResumen.setForeground(bajoMinimo > 0 ? ROJO : getForeground());
        lblResumen.setText(modelo.getRowCount() + " insumos · "
                + (bajoMinimo == 0 ? "ninguno bajo mínimo" : bajoMinimo + " bajo mínimo"));
        habilitarAcciones(insumoSeleccionado().isPresent());
    }
}