package org.example.escritorio.vista.salon;

import org.example.Modelo.menu.Pedido;
import org.example.escritorio.controlador.salon.TicketsControlador;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Formatos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Pantalla de Salón (RF-19 a RF-24): arriba las mesas (tickets de salón), abajo los pedidos de la mesa elegida.
 * Solo dibuja; lo que pasa al hacer clic lo decide TicketsControlador.
 */
public class TicketsPanel extends JPanel {

    private final TicketTableModel modeloTickets = new TicketTableModel();
    private final JTable tablaTickets = new JTable(modeloTickets);
    private final TableRowSorter<TicketTableModel> orden = new TableRowSorter<>(modeloTickets);

    private final PedidoTableModel modeloPedidos = new PedidoTableModel();
    private final JTable tablaPedidos = new JTable(modeloPedidos);

    private final JCheckBox chkSoloAbiertas = new JCheckBox("Solo mesas abiertas", true);
    private final JButton btnActualizar = new JButton("Actualizar");
    private final JButton btnAbrirMesa = new JButton("Abrir mesa");

    private final JLabel lblDetalle = new JLabel("Elegí una mesa para ver sus pedidos.");
    private final JLabel lblTotal = new JLabel(" ");
    private final JButton btnAgregarPedido = new JButton("Agregar pedido");
    private final JLabel lblResumen = new JLabel(" ");

    private final TicketsControlador controlador;

    public TicketsPanel() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, armarTablaTickets(), armarDetalle());
        division.setResizeWeight(0.55);
        division.setBorder(null);

        add(armarEncabezado(), BorderLayout.NORTH);
        add(division, BorderLayout.CENTER);
        add(lblResumen, BorderLayout.SOUTH);

        controlador = new TicketsControlador(this);

        chkSoloAbiertas.addActionListener(e -> aplicarFiltro());
        tablaTickets.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                controlador.alCambiarSeleccion();
            }
        });
        btnActualizar.addActionListener(e -> controlador.cargar(null));
        btnAbrirMesa.addActionListener(e -> controlador.abrirMesa());
        btnAgregarPedido.addActionListener(e -> controlador.agregarPedido());

        habilitarAgregarPedido(false);
        controlador.cargar(null);
    }

    // ---------- armado ----------

    private JPanel armarEncabezado() {
        JLabel titulo = new JLabel("Salón");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.add(chkSoloAbiertas);
        filtros.add(btnActualizar);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(btnAbrirMesa);

        JPanel fila = new JPanel(new BorderLayout());
        fila.add(filtros, BorderLayout.CENTER);
        fila.add(derecha, BorderLayout.EAST);

        JPanel encabezado = new JPanel(new BorderLayout(0, 12));
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(fila, BorderLayout.CENTER);
        return encabezado;
    }

    private JScrollPane armarTablaTickets() {
        tablaTickets.setRowSorter(orden);
        orden.setSortKeys(List.of(new RowSorter.SortKey(TicketTableModel.COL_TICKET, SortOrder.DESCENDING)));
        tablaTickets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaTickets.setFillsViewportHeight(true);
        tablaTickets.setRowHeight(26);
        Formatos.aplicarA(tablaTickets);
        tablaTickets.getColumnModel().getColumn(7).setCellRenderer(new RendererEstado());

        int[] anchos = {60, 70, 200, 120, 90, 70, 110, 100};
        for (int i = 0; i < anchos.length; i++) {
            tablaTickets.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        return new JScrollPane(tablaTickets);
    }

    private JPanel armarDetalle() {
        lblDetalle.setFont(lblDetalle.getFont().deriveFont(Font.BOLD, 14f));

        tablaPedidos.setFillsViewportHeight(true);
        tablaPedidos.setRowHeight(24);
        tablaPedidos.setRowSelectionAllowed(false);
        Formatos.aplicarA(tablaPedidos);
        int[] anchos = {220, 70, 110, 110, 220};
        for (int i = 0; i < anchos.length; i++) {
            tablaPedidos.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 14f));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnAgregarPedido);

        JPanel pie = new JPanel(new BorderLayout());
        pie.add(lblTotal, BorderLayout.WEST);
        pie.add(botones, BorderLayout.EAST);

        JPanel detalle = new JPanel(new BorderLayout(0, 8));
        detalle.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        detalle.add(lblDetalle, BorderLayout.NORTH);
        detalle.add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);
        detalle.add(pie, BorderLayout.SOUTH);
        return detalle;
    }

    // ---------- lo que usa el controlador ----------

    public void mostrarTickets(List<TicketFila> tickets) {
        modeloTickets.setFilas(tickets);
        aplicarFiltro();
    }

    /** Selecciona la fila del ticket pedido, si está visible con el filtro actual. */
    public void seleccionarTicket(int nroTicket) {
        for (int filaVista = 0; filaVista < tablaTickets.getRowCount(); filaVista++) {
            TicketFila t = modeloTickets.getFila(tablaTickets.convertRowIndexToModel(filaVista));
            if (t.nroTicket() == nroTicket) {
                tablaTickets.setRowSelectionInterval(filaVista, filaVista);
                tablaTickets.scrollRectToVisible(tablaTickets.getCellRect(filaVista, 0, true));
                return;
            }
        }
    }

    public Optional<TicketFila> ticketSeleccionado() {
        int filaVista = tablaTickets.getSelectedRow();
        if (filaVista < 0) {
            return Optional.empty();
        }
        return Optional.of(modeloTickets.getFila(tablaTickets.convertRowIndexToModel(filaVista)));
    }

    public void mostrarPedidos(TicketFila ticket, List<Pedido> pedidos) {
        String titulo = "Mesa — Ticket N° " + ticket.nroTicket();
        if (ticket.nroReserva() != null) {
            titulo += " · " + ticket.cliente() + " · " + ticket.zona() + " · " + ticket.comensales() + " comensales";
        }
        lblDetalle.setText(titulo);
        modeloPedidos.setFilas(pedidos);
        BigDecimal total = pedidos.stream().map(Pedido::calcularSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotal.setText(pedidos.isEmpty() ? "Todavía no hay pedidos." : "Total: " + Formatos.pesos(total));
    }

    public void limpiarPedidos() {
        lblDetalle.setText("Elegí una mesa para ver sus pedidos.");
        modeloPedidos.setFilas(List.of());
        lblTotal.setText(" ");
    }

    public void habilitarAgregarPedido(boolean habilitado) {
        btnAgregarPedido.setEnabled(habilitado);
    }

    public JButton getBotonActualizar() { return btnActualizar; }
    public JTable getTablaPedidos() { return tablaPedidos; }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    // ---------- filtro ----------

    private void aplicarFiltro() {
        boolean soloAbiertas = chkSoloAbiertas.isSelected();
        orden.setRowFilter(soloAbiertas ? new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends TicketTableModel, ? extends Integer> fila) {
                return fila.getModel().getFila(fila.getIdentifier()).estaAbierta();
            }
        } : null);

        int abiertas = 0;
        for (int i = 0; i < modeloTickets.getRowCount(); i++) {
            if (modeloTickets.getFila(i).estaAbierta()) {
                abiertas++;
            }
        }
        lblResumen.setText(abiertas + (abiertas == 1 ? " mesa abierta" : " mesas abiertas")
                + " · " + modeloTickets.getRowCount() + " tickets de salón en total");
    }

    /** Estado con color: abierta en verde, facturada en naranja, pagada en gris. */
    private static class RendererEstado extends DefaultTableCellRenderer {
        @Override
        protected void setValue(Object valor) {
            setText(valor == null ? "" : valor.toString());
            if (TicketFila.ABIERTA.equals(valor)) {
                setForeground(new Color(0x2E7D32));
            } else if (TicketFila.FACTURADA.equals(valor)) {
                setForeground(new Color(0xB26A00));
            } else {
                setForeground(Color.GRAY);
            }
        }
    }
}