package org.example.escritorio.vista.caja;

import org.example.Modelo.enums.MetodoPago;
import org.example.Modelo.menu.Pedido;
import org.example.escritorio.controlador.caja.CobrosControlador;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.salon.PedidoTableModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.Optional;

/**
 * Cobros (RF-40 a RF-45): facturar un ticket, aplicar propina, recalcular, confirmar el pago y ver el comprobante.
 * A la izquierda los tickets del restaurante; a la derecha el detalle del elegido.
 */
public class CobrosPanel extends JPanel {

    private final CobroTableModel modelo = new CobroTableModel();
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<CobroTableModel> orden = new TableRowSorter<>(modelo);

    private final PedidoTableModel modeloPedidos = new PedidoTableModel();
    private final JTable tablaPedidos = new JTable(modeloPedidos);

    private final JCheckBox chkSoloPorCobrar = new JCheckBox("Solo por cobrar", true);
    private final JButton btnActualizar = new JButton("Actualizar");
    private final JLabel lblResumen = new JLabel(" ");

    private final JLabel lblDetalle = new JLabel("Elegí un ticket.");
    private final JLabel lblConsumo = new JLabel("—");
    private final JLabel lblPropina = new JLabel("—");
    private final JLabel lblAPagar = new JLabel("—");
    private final JLabel lblMetodo = new JLabel("—");
    private final JLabel lblEstado = new JLabel("—");

    private final JButton btnFacturar = new JButton("Facturar");
    private final JComboBox<Integer> cmbPropina = new JComboBox<>(new Integer[]{5, 10, 15});
    private final JButton btnPropina = new JButton("Aplicar propina");
    private final JButton btnRecalcular = new JButton("Recalcular total");
    private final JButton btnConfirmarPago = new JButton("Confirmar pago");
    private final JButton btnComprobante = new JButton("Comprobante");

    private final CobrosControlador controlador;

    public CobrosPanel() {
        super(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, armarLista(), armarDetalle());
        division.setResizeWeight(0.55);
        division.setBorder(null);
        // la primera vez que la pantalla toma tamaño, la lista ocupa el 55 % del ancho
        division.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                division.setDividerLocation(0.55);
                division.removeComponentListener(this);
            }
        });

        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        arriba.add(chkSoloPorCobrar);
        arriba.add(btnActualizar);

        add(arriba, BorderLayout.NORTH);
        add(division, BorderLayout.CENTER);
        add(lblResumen, BorderLayout.SOUTH);

        controlador = new CobrosControlador(this);

        chkSoloPorCobrar.addActionListener(e -> aplicarFiltro());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                controlador.alCambiarSeleccion();
            }
        });
        btnActualizar.addActionListener(e -> controlador.cargar(null));
        btnFacturar.addActionListener(e -> controlador.facturar());
        btnPropina.addActionListener(e -> controlador.aplicarPropina());
        btnRecalcular.addActionListener(e -> controlador.recalcular());
        btnConfirmarPago.addActionListener(e -> controlador.confirmarPago());
        btnComprobante.addActionListener(e -> controlador.verComprobante());

        mostrarSinSeleccion();
        controlador.cargar(null);
    }

    // ---------- armado ----------

    private JScrollPane armarLista() {
        orden.setSortKeys(List.of(new RowSorter.SortKey(CobroTableModel.COL_TICKET, SortOrder.DESCENDING)));
        tabla.setRowSorter(orden);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(26);
        Formatos.aplicarA(tabla);
        tabla.getColumnModel().getColumn(CobroTableModel.COL_ESTADO).setCellRenderer(new RendererEstado());
        int[] anchos = {55, 70, 170, 60, 100, 100, 120};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        return new JScrollPane(tabla);
    }

    private JPanel armarDetalle() {
        lblDetalle.setFont(lblDetalle.getFont().deriveFont(Font.BOLD, 14f));

        tablaPedidos.setFillsViewportHeight(true);
        tablaPedidos.setRowHeight(24);
        tablaPedidos.setRowSelectionAllowed(false);
        Formatos.aplicarA(tablaPedidos);

        JPanel importes = new JPanel(new GridLayout(0, 2, 12, 4));
        importes.add(new JLabel("Consumo"));
        importes.add(lblConsumo);
        importes.add(new JLabel("Propina"));
        importes.add(lblPropina);
        JLabel etiquetaAPagar = new JLabel("Total a pagar");
        etiquetaAPagar.setFont(etiquetaAPagar.getFont().deriveFont(Font.BOLD));
        lblAPagar.setFont(lblAPagar.getFont().deriveFont(Font.BOLD, 15f));
        importes.add(etiquetaAPagar);
        importes.add(lblAPagar);
        importes.add(new JLabel("Método de pago"));
        importes.add(lblMetodo);
        importes.add(new JLabel("Estado"));
        importes.add(lblEstado);

        JPanel filaPropina = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaPropina.add(new JLabel("Propina %"));
        filaPropina.add(cmbPropina);
        filaPropina.add(btnPropina);
        filaPropina.add(btnRecalcular);

        JPanel filaPago = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaPago.add(btnFacturar);
        filaPago.add(btnConfirmarPago);
        filaPago.add(btnComprobante);

        JPanel acciones = new JPanel(new GridLayout(0, 1, 0, 8));
        acciones.add(filaPago);
        acciones.add(filaPropina);

        JPanel pie = new JPanel(new BorderLayout(0, 12));
        pie.add(importes, BorderLayout.NORTH);
        pie.add(acciones, BorderLayout.CENTER);

        JPanel detalle = new JPanel(new BorderLayout(0, 8));
        detalle.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        detalle.add(lblDetalle, BorderLayout.NORTH);
        detalle.add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);
        detalle.add(pie, BorderLayout.SOUTH);
        return detalle;
    }

    // ---------- lo que usa el controlador ----------

    public void mostrarCobros(List<CobroFila> cobros) {
        modelo.setFilas(cobros);
        aplicarFiltro();
    }

    public Optional<CobroFila> cobroSeleccionado() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return Optional.empty();
        }
        return Optional.of(modelo.getFila(tabla.convertRowIndexToModel(filaVista)));
    }

    public void seleccionarTicket(int nroTicket) {
        for (int fila = 0; fila < tabla.getRowCount(); fila++) {
            if (modelo.getFila(tabla.convertRowIndexToModel(fila)).nroTicket() == nroTicket) {
                tabla.setRowSelectionInterval(fila, fila);
                tabla.scrollRectToVisible(tabla.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    /** Muestra los importes del ticket elegido y habilita solo lo que el backend va a aceptar. */
    public void mostrarCobro(CobroFila c) {
        lblDetalle.setText("Ticket N° " + c.nroTicket() + " · " + c.cliente());
        lblConsumo.setText(Formatos.pesos(c.consumo()));
        lblPropina.setText(c.porcentajePropina() == null ? "Sin propina"
                : Formatos.pesos(c.propina()) + " (" + c.porcentajePropina() + " %)");
        lblAPagar.setText(Formatos.pesos(c.aPagar()));
        lblMetodo.setText(c.metodoPago() != null ? texto(c.metodoPago())
                : c.metodoElegidoPorCliente() != null ? "Eligió " + texto(c.metodoElegidoPorCliente()) : "—");
        lblEstado.setText(c.estado());
        if (c.porcentajePropina() != null) {
            cmbPropina.setSelectedItem(c.porcentajePropina());
        }

        btnFacturar.setEnabled(c.sePuedeFacturar());
        cmbPropina.setEnabled(c.pendiente());
        btnPropina.setEnabled(c.pendiente());
        btnRecalcular.setEnabled(c.pendiente());
        btnConfirmarPago.setEnabled(c.pendiente());
        btnComprobante.setEnabled(!c.sinFacturar());
    }

    public void mostrarPedidos(List<Pedido> pedidos) {
        modeloPedidos.setFilas(pedidos);
    }

    public void mostrarSinSeleccion() {
        lblDetalle.setText("Elegí un ticket.");
        modeloPedidos.setFilas(List.of());
        for (JLabel l : new JLabel[]{lblConsumo, lblPropina, lblAPagar, lblMetodo, lblEstado}) {
            l.setText("—");
        }
        btnFacturar.setEnabled(false);
        cmbPropina.setEnabled(false);
        btnPropina.setEnabled(false);
        btnRecalcular.setEnabled(false);
        btnConfirmarPago.setEnabled(false);
        btnComprobante.setEnabled(false);
    }

    public int getPorcentajePropina() { return (Integer) cmbPropina.getSelectedItem(); }

    public JButton getBotonActualizar() { return btnActualizar; }
    public JButton getBotonFacturar() { return btnFacturar; }
    public JButton getBotonPropina() { return btnPropina; }
    public JButton getBotonRecalcular() { return btnRecalcular; }
    public JButton getBotonConfirmarPago() { return btnConfirmarPago; }
    public JButton getBotonComprobante() { return btnComprobante; }
    public JTable getTablaPedidos() { return tablaPedidos; }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public boolean confirmar(String pregunta) {
        return Dialogos.confirmar(this, pregunta);
    }

    /** Efectivo, Tarjeta, QR. */
    public static String texto(MetodoPago metodo) {
        return switch (metodo) {
            case EFECTIVO -> "Efectivo";
            case TARJETA -> "Tarjeta";
            case QR -> "QR";
        };
    }

    // ---------- filtro ----------

    private void aplicarFiltro() {
        orden.setRowFilter(chkSoloPorCobrar.isSelected() ? new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends CobroTableModel, ? extends Integer> fila) {
                return fila.getModel().getFila(fila.getIdentifier()).porCobrar();
            }
        } : null);

        int sinFacturar = 0;
        int pendientes = 0;
        for (int i = 0; i < modelo.getRowCount(); i++) {
            CobroFila c = modelo.getFila(i);
            if (c.sePuedeFacturar()) {
                sinFacturar++;
            } else if (c.pendiente()) {
                pendientes++;
            }
        }
        lblResumen.setText(sinFacturar + " para facturar · " + pendientes
                + (pendientes == 1 ? " pendiente de pago" : " pendientes de pago"));
    }

    /** Sin facturar en azul, pendiente en naranja, pagado en verde. */
    private static class RendererEstado extends DefaultTableCellRenderer {
        @Override
        protected void setValue(Object valor) {
            setText(valor == null ? "" : valor.toString());
            if (CobroFila.SIN_FACTURAR.equals(valor)) {
                setForeground(new Color(0x1565C0));
            } else if (CobroFila.PENDIENTE.equals(valor)) {
                setForeground(new Color(0xB26A00));
            } else {
                setForeground(new Color(0x2E7D32));
            }
        }
    }
}