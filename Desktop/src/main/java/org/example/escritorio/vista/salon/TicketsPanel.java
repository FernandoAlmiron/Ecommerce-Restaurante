package org.example.escritorio.vista.salon;

import org.example.Modelo.menu.Pedido;
import org.example.escritorio.controlador.salon.TicketsControlador;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Icono;
import org.example.escritorio.vista.comun.Tablas;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Pantalla de Salón (RF-19 a RF-24): arriba las mesas (tickets de salón), abajo los pedidos de la mesa elegida.
 * Solo dibuja; lo que pasa al hacer clic lo decide TicketsControlador.
 */
public class TicketsPanel extends JPanel {

    private static final int ALTO_FILA = 56;

    private final TicketTableModel modeloTickets = new TicketTableModel();
    private final JTable tablaTickets = new JTable(modeloTickets);
    private final TableRowSorter<TicketTableModel> orden = new TableRowSorter<>(modeloTickets);

    private final PedidoTableModel modeloPedidos = new PedidoTableModel();
    private final JTable tablaPedidos = new JTable(modeloPedidos);

    private final JCheckBox chkSoloAbiertas = new JCheckBox("Solo mesas abiertas", true);
    private final JButton btnActualizar = new Boton(null, new Icono(Icono.Tipo.ACTUALIZAR, 20, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);
    private final JButton btnAbrirMesa = new Boton("Abrir mesa", new Icono(Icono.Tipo.MAS, 16, Color.WHITE),
            Boton.Tipo.PRIMARIO);

    private final JLabel lblDetalle = new JLabel("Pedidos");
    private final JLabel lblDetalleDatos = new JLabel("Elegí una mesa para ver sus pedidos.");
    private final JLabel lblTotal = new JLabel(" ");
    private final JPanel cabeceraDetalle = Estilo.transparente(new BorderLayout());
    private final JPanel pieDetalle = new JPanel(new BorderLayout(28, 0));
    private final JButton btnAgregarPedido = new Boton("Agregar pedido", new Icono(Icono.Tipo.MAS, 16, Color.WHITE),
            Boton.Tipo.OSCURO);
    private final JLabel lblResumen = new JLabel(" ");
    private final JLabel lblResumenDetalle = new JLabel(" ");

    private final TicketsControlador controlador;

    public TicketsPanel() {
        super(new BorderLayout(0, 24));
        setBackground(Estilo.FONDO);
        setBorder(Estilo.relleno(30, 48, 28, 48));

        // las dos tarjetas miden lo que ocupa su contenido; si no entran, se reparten el alto
        JPanel centro = Estilo.transparente(new Tablas.Apilado(28,
                () -> Tablas.altoContenido(tablaTickets),
                () -> cabeceraDetalle.getPreferredSize().height + Tablas.altoContenido(tablaPedidos)
                        + (pieDetalle.isVisible() ? pieDetalle.getPreferredSize().height : 0) + 2));
        centro.add(armarTablaTickets());
        centro.add(armarDetalle());

        add(armarEncabezado(), BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(armarResumen(), BorderLayout.SOUTH);

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
        limpiarPedidos();
        controlador.cargar(null);
    }

    // ---------- armado ----------

    private JComponent armarEncabezado() {
        chkSoloAbiertas.setFont(Estilo.fuente(Font.PLAIN, 16f));
        chkSoloAbiertas.setForeground(Estilo.TEXTO);
        chkSoloAbiertas.setOpaque(false);
        chkSoloAbiertas.setIconTextGap(10);
        chkSoloAbiertas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Estilo.flat(chkSoloAbiertas, "icon.selectedBackground: #8C5E35; icon.selectedBorderColor: #8C5E35;"
                + " icon.checkmarkColor: #FFFFFF; icon.focusWidth: 0");

        // el checkbox va dentro de una cajita blanca con borde, como los botones
        Estilo.Tarjeta cajaFiltro = new Estilo.Tarjeta(new GridBagLayout(), 14);
        cajaFiltro.setBorder(Estilo.relleno(9, 16, 9, 18));
        cajaFiltro.add(chkSoloAbiertas);

        btnActualizar.setToolTipText("Actualizar");
        btnActualizar.setBorder(Estilo.relleno(12, 12, 12, 12));

        JPanel derecha = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(cajaFiltro);
        derecha.add(Box.createHorizontalStrut(14));
        derecha.add(btnActualizar);
        derecha.add(Box.createHorizontalStrut(14));
        derecha.add(btnAbrirMesa);

        JPanel centrado = Estilo.transparente(new GridBagLayout());
        centrado.add(derecha);

        JPanel encabezado = Estilo.transparente(new BorderLayout());
        encabezado.add(Estilo.tituloPantalla("Salón", "Mesas abiertas"), BorderLayout.CENTER);
        encabezado.add(centrado, BorderLayout.EAST);
        return encabezado;
    }

    private JComponent armarTablaTickets() {
        tablaTickets.setRowSorter(orden);
        orden.setSortKeys(List.of(new RowSorter.SortKey(TicketTableModel.COL_TICKET, SortOrder.DESCENDING)));
        tablaTickets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Tablas.estilizar(tablaTickets, ALTO_FILA, Set.of(4, 5, 6));

        Font normal = Estilo.fuente(Font.PLAIN, 16f);
        Font negrita = Estilo.fuente(Font.BOLD, 16f);
        Tablas.renderer(tablaTickets, 0, new Tablas.RendererTexto(normal, Estilo.TEXTO_SUAVE, v -> "#T-" + v));
        Tablas.renderer(tablaTickets, 1, new Tablas.RendererTexto(normal, Estilo.TEXTO_SUAVE, v -> "#" + v));
        Tablas.renderer(tablaTickets, 2, new RendererCliente(normal));
        Tablas.renderer(tablaTickets, 3, new Tablas.RendererTexto(normal, Estilo.TEXTO, String::valueOf));
        Tablas.renderer(tablaTickets, 4, new Tablas.RendererTexto(normal, Estilo.TEXTO, String::valueOf, true));
        Tablas.renderer(tablaTickets, 5, new Tablas.RendererTexto(normal, Estilo.TEXTO, String::valueOf, true));
        Tablas.renderer(tablaTickets, 6, new RendererTotal(negrita, normal));
        Tablas.renderer(tablaTickets, 7, new Tablas.RendererInsignia(v -> null) {
            @Override
            protected Tablas.Insignia insigniaPara(JTable tabla, Object valor, int filaVista) {
                TicketFila t = modeloTickets.getFila(tabla.convertRowIndexToModel(filaVista));
                if (t.estaAbierta()) {
                    return t.cantidadPedidos() == 0 ? Tablas.Insignia.gris("Sin pedidos")
                            : Tablas.Insignia.verde("Abierta");
                }
                return TicketFila.FACTURADA.equals(t.estado()) ? Tablas.Insignia.violeta("Facturada")
                        : Tablas.Insignia.gris("Pagada");
            }
        });
        Tablas.anchos(tablaTickets, 110, 110, 220, 130, 150, 120, 150, 170);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(Tablas.scroll(tablaTickets), BorderLayout.CENTER);
        return tarjeta;
    }

    private JComponent armarDetalle() {
        lblDetalle.setFont(Estilo.fuente(Font.BOLD, 22f));
        lblDetalle.setForeground(Estilo.TEXTO);
        lblDetalleDatos.setFont(Estilo.fuente(Font.PLAIN, 15f));
        lblDetalleDatos.setForeground(Estilo.TEXTO_SUAVE);

        JPanel titulos = Estilo.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titulos.add(lblDetalle);
        titulos.add(Box.createHorizontalStrut(14));
        titulos.add(lblDetalleDatos);
        JPanel titulosCentrados = Estilo.transparente(new GridBagLayout());
        titulosCentrados.add(titulos);

        cabeceraDetalle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),
                Estilo.relleno(14, Tablas.RELLENO_CELDA, 14, Tablas.RELLENO_CELDA)));
        cabeceraDetalle.add(titulosCentrados, BorderLayout.WEST);
        cabeceraDetalle.add(btnAgregarPedido, BorderLayout.EAST);

        tablaPedidos.setRowSelectionAllowed(false);
        Tablas.estilizar(tablaPedidos, ALTO_FILA, Set.of(1, 2, 3));
        Font normal = Estilo.fuente(Font.PLAIN, 16f);
        Font negrita = Estilo.fuente(Font.BOLD, 16f);
        Tablas.renderer(tablaPedidos, 0, new Tablas.RendererTexto(negrita, Estilo.TEXTO, String::valueOf));
        Tablas.renderer(tablaPedidos, 1, new Tablas.RendererTexto(normal, Estilo.TEXTO, String::valueOf, true));
        Tablas.renderer(tablaPedidos, 2, new Tablas.RendererTexto(normal, Estilo.TEXTO,
                v -> Tablas.pesosCortos((BigDecimal) v), true));
        Tablas.renderer(tablaPedidos, 3, new Tablas.RendererTexto(normal, Estilo.TEXTO,
                v -> Tablas.pesosCortos((BigDecimal) v), true));
        Tablas.renderer(tablaPedidos, 4, new Tablas.RendererTexto(normal, Estilo.TEXTO_SUAVE, String::valueOf));
        Tablas.anchos(tablaPedidos, 320, 120, 190, 150, 280);

        JLabel etiquetaTotal = new JLabel("Total de la mesa");
        etiquetaTotal.setFont(Estilo.fuente(Font.PLAIN, 16f));
        etiquetaTotal.setForeground(Estilo.TEXTO_SUAVE);
        lblTotal.setFont(Estilo.fuente(Font.BOLD, 24f));
        lblTotal.setForeground(Estilo.TEXTO);
        // margen para que la negrita grande no quede recortada en el último dígito
        lblTotal.setBorder(Estilo.relleno(0, 0, 0, 6));

        etiquetaTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        pieDetalle.setBackground(Estilo.ENCABEZADO_TABLA);
        pieDetalle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDE),
                Estilo.relleno(16, Tablas.RELLENO_CELDA, 16, Tablas.RELLENO_CELDA - 6)));
        pieDetalle.add(etiquetaTotal, BorderLayout.CENTER);
        pieDetalle.add(lblTotal, BorderLayout.EAST);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(cabeceraDetalle, BorderLayout.NORTH);
        tarjeta.add(Tablas.scroll(tablaPedidos), BorderLayout.CENTER);
        tarjeta.add(pieDetalle, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JComponent armarResumen() {
        lblResumen.setFont(Estilo.fuente(Font.BOLD, 16f));
        lblResumen.setForeground(Estilo.TEXTO);
        lblResumenDetalle.setFont(Estilo.fuente(Font.PLAIN, 16f));
        lblResumenDetalle.setForeground(Estilo.TEXTO_SUAVE);

        JPanel resumen = Estilo.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        resumen.add(lblResumen);
        resumen.add(lblResumenDetalle);
        return resumen;
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
        lblDetalle.setText(ticket.nroReserva() != null
                ? "Pedidos · " + ticket.cliente()
                : "Pedidos · Ticket #T-" + ticket.nroTicket());
        StringBuilder datos = new StringBuilder("#T-" + ticket.nroTicket());
        if (ticket.comensales() != null) {
            datos.append(" · ").append(ticket.comensales()).append(" comensales");
        }
        if (ticket.zona() != null) {
            datos.append(" · ").append(ticket.zona());
        }
        if (pedidos.isEmpty()) {
            datos.append(" · Todavía no hay pedidos");
        }
        lblDetalleDatos.setText(datos.toString());
        modeloPedidos.setFilas(pedidos);
        BigDecimal total = pedidos.stream().map(Pedido::calcularSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotal.setText(Tablas.pesosCortos(total));
        pieDetalle.setVisible(true);
        refrescarAlto();
    }

    public void limpiarPedidos() {
        lblDetalle.setText("Pedidos");
        lblDetalleDatos.setText("Elegí una mesa para ver sus pedidos.");
        modeloPedidos.setFilas(List.of());
        lblTotal.setText(" ");
        pieDetalle.setVisible(false);
        refrescarAlto();
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
        int comensales = 0;
        for (int i = 0; i < modeloTickets.getRowCount(); i++) {
            TicketFila t = modeloTickets.getFila(i);
            if (t.estaAbierta()) {
                abiertas++;
                comensales += t.comensales() == null ? 0 : t.comensales();
            }
        }
        lblResumen.setText(abiertas + (abiertas == 1 ? " mesa abierta" : " mesas abiertas"));
        lblResumenDetalle.setText(" · " + comensales + " comensales · "
                + modeloTickets.getRowCount() + " tickets de salón en total");
        refrescarAlto();
    }

    /** Las tarjetas miden lo que ocupan las filas: al cambiar los datos se vuelve a acomodar. */
    private void refrescarAlto() {
        revalidate();
        repaint();
    }

    // ---------- celdas ----------

    /** Cliente; los tickets sin reserva van en gris. */
    private class RendererCliente extends Tablas.RendererTexto {
        RendererCliente(Font fuente) {
            super(fuente, Estilo.TEXTO, String::valueOf);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
            TicketFila t = modeloTickets.getFila(tabla.convertRowIndexToModel(fila));
            if (t.nroReserva() == null) {
                setForeground(Estilo.TEXTO_SUAVE);
            }
            if (seleccionada) {
                setFont(Estilo.fuente(Font.BOLD, 16f));
            }
            return this;
        }
    }

    /** Total en negrita; $ 0 en gris. */
    private static class RendererTotal extends Tablas.RendererTexto {
        private final Font negrita;
        private final Font normal;

        RendererTotal(Font negrita, Font normal) {
            super(negrita, Estilo.TEXTO, v -> Tablas.pesosCortos((BigDecimal) v), true);
            this.negrita = negrita;
            this.normal = normal;
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
            boolean cero = valor == null || ((BigDecimal) valor).signum() == 0;
            setFont(cero ? normal : negrita);
            setForeground(cero ? Estilo.TEXTO_SUAVE : Estilo.TEXTO);
            return this;
        }
    }
}
