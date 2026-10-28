package org.example.escritorio.vista.caja;

import org.example.Modelo.enums.MetodoPago;
import org.example.Modelo.enums.OrigenTicket;
import org.example.Modelo.menu.Pedido;
import org.example.escritorio.controlador.caja.CobrosControlador;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Icono;
import org.example.escritorio.vista.comun.Selector;
import org.example.escritorio.vista.comun.Tablas;
import org.example.escritorio.vista.comun.TarjetaDato;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Cobros (RF-40 a RF-45): facturar un ticket, aplicar propina, recalcular, confirmar el pago y ver el comprobante.
 * Arriba los números del día; a la izquierda los tickets del restaurante y a la derecha el detalle del elegido.
 */
public class CobrosPanel extends JPanel {

    /** Propiedad que avisa cuántos tickets hay por cobrar (la usa la solapa para su contador). */
    public static final String PROP_POR_COBRAR = "ticketsPorCobrar";

    private static final int ALTO_FILA = 56;
    private static final int[] PROPINAS = {5, 10, 15};

    private final CobroTableModel modelo = new CobroTableModel();
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<CobroTableModel> orden = new TableRowSorter<>(modelo);

    private final TarjetaDato datoCobrado = new TarjetaDato("Cobrado hoy", false);
    private final TarjetaDato datoPorCobrar = new TarjetaDato("Por cobrar", false);
    private final TarjetaDato datoCerrados = new TarjetaDato("Tickets cobrados hoy", false);
    private final TarjetaDato datoPropinas = new TarjetaDato("Propinas de hoy", false);

    private final Selector selFiltro = new Selector("Por cobrar", "Todos");
    private final JButton btnActualizar = new Boton(null, new Icono(Icono.Tipo.ACTUALIZAR, 20, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);
    private final JLabel lblResumen = new JLabel(" ");
    private final JLabel lblResumenDetalle = new JLabel(" ");

    // detalle del ticket elegido
    private final JLabel lblNumero = new JLabel(" ");
    private final JLabel lblTitulo = new JLabel(" ");
    private final JLabel lblDatos = new JLabel(" ");
    private final Estilo.Pildora lblEstado = new Estilo.Pildora("", Estilo.AMARILLO_FONDO, Estilo.AMARILLO_TEXTO,
            Estilo.fuente(Font.BOLD, 15f), -1);
    private final JPanel panelPedidos = Estilo.transparente(null);
    private final JLabel lblConsumo = new JLabel("—");
    private final JLabel lblPropina = new JLabel("—");
    private final JLabel lblAPagar = new JLabel("—");
    private final ChipPropina[] chipsPropina = new ChipPropina[PROPINAS.length];
    private final JPanel cuerpoDetalle = Estilo.transparente(new BorderLayout());

    private final JButton btnRecalcular = new Boton(null, new Icono(Icono.Tipo.ACTUALIZAR, 14, Estilo.TEXTO_SUAVE),
            Boton.Tipo.CONTORNO);
    private final JButton btnFacturar = new Boton("Facturar", Boton.Tipo.CONTORNO);
    private final JButton btnComprobante = new Boton("Comprobante", Boton.Tipo.CONTORNO);
    private final JButton btnConfirmarPago = new Boton("Confirmar pago", new Icono(Icono.Tipo.TILDE, 16, Color.WHITE),
            Boton.Tipo.PRIMARIO);

    /** Porcentaje de la propina que se va a aplicar y el chip que lo pidió (origen de la tarea). */
    private int porcentajePropina = 10;
    private JButton botonPropina = new JButton();

    private final CobrosControlador controlador;

    public CobrosPanel() {
        super(new BorderLayout(0, 22));
        setOpaque(false);
        setBorder(Estilo.relleno(4, 0, 0, 0));

        add(armarDatos(), BorderLayout.NORTH);
        add(armarCentro(), BorderLayout.CENTER);
        add(armarResumen(), BorderLayout.SOUTH);

        controlador = new CobrosControlador(this);

        selFiltro.alElegir(i -> aplicarFiltro());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                controlador.alCambiarSeleccion();
            }
        });
        btnActualizar.addActionListener(e -> controlador.cargar(null));
        btnFacturar.addActionListener(e -> controlador.facturar());
        btnRecalcular.addActionListener(e -> controlador.recalcular());
        btnConfirmarPago.addActionListener(e -> controlador.confirmarPago());
        btnComprobante.addActionListener(e -> controlador.verComprobante());

        mostrarSinSeleccion();
        controlador.cargar(null);
    }

    // ---------- armado ----------

    private JComponent armarDatos() {
        datoPorCobrar.setColorValor(Estilo.AMARILLO_TEXTO);
        JPanel datos = Estilo.transparente(new GridLayout(1, 4, 20, 0));
        datos.add(datoCobrado);
        datos.add(datoPorCobrar);
        datos.add(datoCerrados);
        datos.add(datoPropinas);
        return datos;
    }

    private JComponent armarCentro() {
        JComponent lista = armarLista();
        JComponent detalle = armarDetalle();
        // ancho preferido mínimo: así las dos columnas se reparten el lugar según weightx
        lista.setPreferredSize(new Dimension(100, 100));
        detalle.setPreferredSize(new Dimension(100, 100));

        JPanel centro = Estilo.transparente(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.weightx = 0.54;
        c.insets = new Insets(0, 0, 0, 14);
        centro.add(lista, c);
        c.gridx = 1;
        c.weightx = 0.46;
        c.insets = new Insets(0, 14, 0, 0);
        centro.add(detalle, c);
        return centro;
    }

    private JComponent armarLista() {
        orden.setSortKeys(List.of(new RowSorter.SortKey(CobroTableModel.COL_TICKET, SortOrder.DESCENDING)));
        tabla.setRowSorter(orden);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Tablas.estilizar(tabla, ALTO_FILA);

        Font normal = Estilo.fuente(Font.PLAIN, 16f);
        Font negrita = Estilo.fuente(Font.BOLD, 16f);
        Tablas.renderer(tabla, 0, new Tablas.RendererTexto(normal, Estilo.TEXTO_SUAVE, v -> "#" + v));
        Tablas.renderer(tabla, 1, new Tablas.RendererTexto(normal, Estilo.TEXTO, String::valueOf));
        Tablas.renderer(tabla, 2, new Tablas.RendererTexto(normal, Estilo.TEXTO, String::valueOf));
        Tablas.renderer(tabla, 5, new Tablas.RendererTexto(negrita, Estilo.TEXTO,
                v -> Tablas.pesosCortos((BigDecimal) v)));
        Tablas.renderer(tabla, CobroTableModel.COL_ESTADO, new Tablas.RendererInsignia(CobrosPanel::insignia));
        Tablas.anchos(tabla, 80, 100, 125, 80, 120, 105, 150);
        // la lista muestra lo que caja necesita: pedidos y consumo están en el detalle
        tabla.removeColumn(tabla.getColumnModel().getColumn(4));
        tabla.removeColumn(tabla.getColumnModel().getColumn(3));

        btnActualizar.setToolTipText("Actualizar");
        btnActualizar.setBorder(Estilo.relleno(12, 12, 12, 12));
        JPanel filtros = Estilo.transparente(new BorderLayout());
        filtros.add(selFiltro, BorderLayout.WEST);
        filtros.add(btnActualizar, BorderLayout.EAST);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(Tablas.scroll(tabla), BorderLayout.CENTER);

        JPanel lista = Estilo.transparente(new BorderLayout(0, 16));
        lista.add(filtros, BorderLayout.NORTH);
        lista.add(tarjeta, BorderLayout.CENTER);
        return lista;
    }

    private JComponent armarDetalle() {
        lblNumero.setFont(Estilo.conEspaciado(Estilo.fuente(Font.BOLD, 14f), 0.1f));
        lblNumero.setForeground(Estilo.CAFE);
        lblNumero.setBorder(Estilo.relleno(0, 0, 0, 8));
        lblTitulo.setFont(Estilo.fuente(Font.BOLD, 24f));
        lblTitulo.setForeground(Estilo.TEXTO);
        // margen para que la negrita no quede recortada en la última letra
        lblTitulo.setBorder(Estilo.relleno(0, 0, 0, 6));
        lblDatos.setFont(Estilo.fuente(Font.PLAIN, 15f));
        lblDatos.setForeground(Estilo.TEXTO_SUAVE);
        lblEstado.setBorder(Estilo.relleno(7, 16, 7, 16));

        JPanel textos = Estilo.transparente(null);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(lblNumero);
        textos.add(Box.createVerticalStrut(4));
        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(lblDatos);
        JPanel estado = Estilo.transparente(new BorderLayout());
        estado.add(lblEstado, BorderLayout.NORTH);

        JPanel cabecera = Estilo.transparente(new BorderLayout(12, 0));
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                new Estilo.LineaPunteada(false),
                Estilo.relleno(24, 28, 18, 28)));
        cabecera.add(textos, BorderLayout.CENTER);
        cabecera.add(estado, BorderLayout.EAST);

        panelPedidos.setLayout(new BoxLayout(panelPedidos, BoxLayout.Y_AXIS));
        panelPedidos.setBorder(Estilo.relleno(4, 28, 4, 28));
        JPanel pedidosArriba = Estilo.transparente(new BorderLayout());
        pedidosArriba.add(panelPedidos, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(pedidosArriba);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel abajo = Estilo.transparente(new BorderLayout(0, 18));
        abajo.setBorder(Estilo.relleno(10, 28, 22, 28));
        abajo.add(armarTotales(), BorderLayout.CENTER);
        abajo.add(armarBotones(), BorderLayout.SOUTH);

        cuerpoDetalle.add(scroll, BorderLayout.CENTER);
        cuerpoDetalle.add(abajo, BorderLayout.SOUTH);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(cabecera, BorderLayout.NORTH);
        tarjeta.add(cuerpoDetalle, BorderLayout.CENTER);
        return tarjeta;
    }

    /** Caja crema con consumo, propina, total a pagar y método de pago. */
    private JComponent armarTotales() {
        Font normal = Estilo.fuente(Font.PLAIN, 16f);
        for (JLabel l : new JLabel[]{lblConsumo, lblPropina}) {
            l.setFont(normal);
            l.setForeground(Estilo.TEXTO);
            l.setHorizontalAlignment(JLabel.RIGHT);
        }
        lblAPagar.setFont(Estilo.fuente(Font.BOLD, 30f));
        lblAPagar.setForeground(Estilo.CAFE_OSCURO);
        lblAPagar.setBorder(Estilo.relleno(0, 0, 0, 6));

        btnRecalcular.setToolTipText("Recalcular el total con los pedidos actuales");
        btnRecalcular.setBorder(Estilo.relleno(5, 5, 5, 5));
        ((Boton) btnRecalcular).conArco(10);

        JPanel chips = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        for (int i = 0; i < PROPINAS.length; i++) {
            int porcentaje = PROPINAS[i];
            ChipPropina chip = new ChipPropina(porcentaje + "%");
            chip.setToolTipText("Aplicar propina del " + porcentaje + " %");
            chip.addActionListener(e -> {
                porcentajePropina = porcentaje;
                botonPropina = chip;
                controlador.aplicarPropina();
            });
            chipsPropina[i] = chip;
            chips.add(chip);
        }

        JPanel caja = new Estilo.Tarjeta(new GridBagLayout(), 18);
        caja.setBackground(Estilo.FILA_SELECCIONADA);
        caja.setBorder(Estilo.relleno(16, 22, 16, 22));
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        filaTotal(caja, c, "Consumo", btnRecalcular, lblConsumo);
        c.gridy = 1;
        filaTotal(caja, c, "Propina", chips, lblPropina);

        c.gridy = 3;
        c.gridx = 0;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 0, 10, 0);
        JPanel punteada = Estilo.transparente(null);
        punteada.setBorder(new Estilo.LineaPunteada(true));
        punteada.setPreferredSize(new Dimension(10, 1));
        caja.add(punteada, c);

        c.gridy = 4;
        c.gridwidth = 1;
        c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        JLabel total = new JLabel("Total a pagar");
        total.setFont(Estilo.fuente(Font.BOLD, 17f));
        total.setBorder(Estilo.relleno(0, 0, 0, 6)); // la negrita no tiene que quedar recortada
        total.setForeground(Estilo.TEXTO);
        caja.add(total, c);
        c.gridx = 1;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.EAST;
        caja.add(lblAPagar, c);
        return caja;
    }

    private static void filaTotal(JPanel caja, GridBagConstraints c, String nombre, JComponent medio, JLabel valor) {
        JLabel lbl = new JLabel(nombre);
        lbl.setFont(Estilo.fuente(Font.PLAIN, 16f));
        lbl.setForeground(Estilo.TEXTO_SUAVE);
        c.gridx = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(4, 0, 4, 12);
        caja.add(lbl, c);
        c.gridx = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        if (medio != null) {
            caja.add(medio, c);
        } else {
            caja.add(Box.createHorizontalStrut(1), c);
        }
        c.gridx = 2;
        c.weightx = 0;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(4, 12, 4, 0);
        caja.add(valor, c);
    }

    private JComponent armarBotones() {
        // arriba Comprobante y Facturar; abajo, a todo el ancho, la acción principal
        btnConfirmarPago.setBorder(Estilo.relleno(14, 20, 14, 22));
        btnConfirmarPago.setFont(Estilo.fuente(Font.BOLD, 16f));
        JPanel secundarios = Estilo.transparente(new GridLayout(1, 2, 10, 0));
        secundarios.add(btnComprobante);
        secundarios.add(btnFacturar);
        JPanel botones = Estilo.transparente(new BorderLayout(0, 10));
        botones.add(secundarios, BorderLayout.NORTH);
        botones.add(btnConfirmarPago, BorderLayout.CENTER);
        return botones;
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

    public void mostrarCobros(List<CobroFila> cobros) {
        modelo.setFilas(cobros);
        aplicarFiltro();
        actualizarDatos(cobros);
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
        cuerpoDetalle.setVisible(true);
        lblNumero.setText("TICKET N° " + c.nroTicket());
        lblTitulo.setText(c.cliente() + " · " + origen(c));
        String metodo = c.metodoPago() != null ? texto(c.metodoPago())
                : c.metodoElegidoPorCliente() != null ? "eligió " + texto(c.metodoElegidoPorCliente())
                : "método al facturar";
        lblDatos.setText(c.cantidadPedidos() + (c.cantidadPedidos() == 1 ? " pedido" : " pedidos")
                + (c.nroFacturacion() != null ? " · factura N° " + c.nroFacturacion() : " · sin facturar")
                + " · " + metodo);
        Tablas.Insignia i = insignia(c.estado());
        lblEstado.setText(i.texto());
        lblEstado.setFondo(i.fondo());
        lblEstado.setForeground(i.color());
        lblEstado.setVisible(true);

        lblConsumo.setText(Tablas.pesosCortos(c.consumo()));
        lblPropina.setText(c.porcentajePropina() == null ? "Sin propina" : Tablas.pesosCortos(c.propina()));
        lblAPagar.setText(Tablas.pesosCortos(c.aPagar()));
        for (int k = 0; k < PROPINAS.length; k++) {
            chipsPropina[k].setElegido(c.porcentajePropina() != null && c.porcentajePropina() == PROPINAS[k]);
            chipsPropina[k].setEnabled(c.pendiente());
        }
        btnConfirmarPago.setText(c.pendiente() ? "Confirmar pago · " + Tablas.pesosCortos(c.aPagar())
                : "Confirmar pago");

        btnFacturar.setEnabled(c.sePuedeFacturar());
        btnRecalcular.setEnabled(c.pendiente());
        btnConfirmarPago.setEnabled(c.pendiente());
        btnComprobante.setEnabled(!c.sinFacturar());
    }

    public void mostrarPedidos(List<Pedido> pedidos) {
        panelPedidos.removeAll();
        for (int i = 0; i < pedidos.size(); i++) {
            if (i > 0) {
                JPanel linea = new JPanel();
                linea.setBackground(Estilo.BORDE);
                linea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                linea.setPreferredSize(new Dimension(1, 1));
                linea.setAlignmentX(LEFT_ALIGNMENT);
                panelPedidos.add(linea);
            }
            panelPedidos.add(new FilaPedido(pedidos.get(i)));
        }
        panelPedidos.revalidate();
        panelPedidos.repaint();
    }

    public void mostrarSinSeleccion() {
        lblNumero.setText("COBROS");
        lblTitulo.setText("Elegí un ticket");
        lblDatos.setText("Acá vas a ver sus pedidos, la propina y el total a pagar.");
        lblEstado.setVisible(false);
        cuerpoDetalle.setVisible(false);
        panelPedidos.removeAll();
        btnFacturar.setEnabled(false);
        btnRecalcular.setEnabled(false);
        btnConfirmarPago.setEnabled(false);
        btnComprobante.setEnabled(false);
        for (ChipPropina chip : chipsPropina) {
            chip.setElegido(false);
            chip.setEnabled(false);
        }
    }

    public int getPorcentajePropina() { return porcentajePropina; }

    public JButton getBotonActualizar() { return btnActualizar; }
    public JButton getBotonFacturar() { return btnFacturar; }
    /** El chip de propina que se tocó (se deshabilita mientras se aplica). */
    public JButton getBotonPropina() { return botonPropina; }
    public JButton getBotonRecalcular() { return btnRecalcular; }
    public JButton getBotonConfirmarPago() { return btnConfirmarPago; }
    public JButton getBotonComprobante() { return btnComprobante; }
    /** La lista de pedidos del detalle (se deshabilita mientras se cargan). */
    public JComponent getTablaPedidos() { return panelPedidos; }

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

    // ---------- filtro y totales ----------

    private void aplicarFiltro() {
        boolean soloPorCobrar = selFiltro.getElegida() == 0;
        orden.setRowFilter(soloPorCobrar ? new RowFilter<>() {
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
        lblResumen.setText(pendientes + (pendientes == 1 ? " por cobrar" : " por cobrar"));
        lblResumenDetalle.setText(" · " + sinFacturar + " para facturar");
        firePropertyChange(PROP_POR_COBRAR, -1, pendientes + sinFacturar);
    }

    /** Los números de arriba: lo cobrado hoy (pagos con fecha de hoy) y lo que falta cobrar. */
    private void actualizarDatos(List<CobroFila> cobros) {
        LocalDate hoy = LocalDate.now();
        BigDecimal cobrado = BigDecimal.ZERO;
        BigDecimal porCobrar = BigDecimal.ZERO;
        BigDecimal propinas = BigDecimal.ZERO;
        int cerrados = 0;
        for (CobroFila c : cobros) {
            if (c.pagadoEl(hoy)) {
                cobrado = cobrado.add(c.aPagar());
                propinas = propinas.add(c.propina() == null ? BigDecimal.ZERO : c.propina());
                cerrados++;
            } else if (c.porCobrar()) {
                porCobrar = porCobrar.add(c.aPagar());
            }
        }
        datoCobrado.setValor(Tablas.pesosCortos(cobrado));
        datoPorCobrar.setValor(Tablas.pesosCortos(porCobrar));
        datoCerrados.setValor(String.valueOf(cerrados));
        datoPropinas.setValor(Tablas.pesosCortos(propinas));
    }

    private static String origen(CobroFila c) {
        return c.origen() == OrigenTicket.DELIVERY ? "Delivery" : "Salón";
    }

    /** Para facturar en violeta, por cobrar en amarillo, pagado en verde. */
    private static Tablas.Insignia insignia(Object estado) {
        if (CobroFila.SIN_FACTURAR.equals(estado)) {
            return Tablas.Insignia.violeta("Para facturar");
        }
        if (CobroFila.PENDIENTE.equals(estado)) {
            return Tablas.Insignia.amarilla("Por cobrar");
        }
        return Tablas.Insignia.verde("Pagado");
    }

    // ---------- piezas visuales ----------

    /** "2×  Ñoquis de papa  .......  $ 38.400" */
    private static class FilaPedido extends JPanel {
        FilaPedido(Pedido p) {
            super(new BorderLayout(14, 0));
            setOpaque(false);
            setBorder(Estilo.relleno(12, 0, 12, 0));
            setAlignmentX(LEFT_ALIGNMENT);

            JLabel cantidad = new JLabel(p.getCantidad() + "×");
            cantidad.setFont(Estilo.fuente(Font.BOLD, 16f));
            cantidad.setForeground(Estilo.CAFE);
            cantidad.setPreferredSize(new Dimension(36, cantidad.getPreferredSize().height));

            String nombre = p.getMenu() != null ? p.getMenu().getNombre() : "—";
            if (p.getObservaciones() != null && !p.getObservaciones().isBlank()) {
                nombre += "  ·  " + p.getObservaciones();
            }
            JLabel plato = new JLabel(nombre);
            plato.setFont(Estilo.fuente(Font.PLAIN, 16f));
            plato.setForeground(Estilo.TEXTO);

            JLabel subtotal = new JLabel(Tablas.pesosCortos(p.calcularSubtotal()));
            subtotal.setFont(Estilo.fuente(Font.PLAIN, 16f));
            subtotal.setForeground(Estilo.TEXTO);

            add(cantidad, BorderLayout.WEST);
            add(plato, BorderLayout.CENTER);
            add(subtotal, BorderLayout.EAST);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }

    /** Botoncito redondo de propina (5 %, 10 %, 15 %): café cuando es la propina aplicada. */
    private static class ChipPropina extends JButton {
        private boolean elegido;

        ChipPropina(String texto) {
            super(texto);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(Estilo.fuente(Font.PLAIN, 14f));
            setBorder(Estilo.relleno(6, 12, 6, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            putClientProperty("JButton.buttonType", "borderless");
            setElegido(false);
        }

        void setElegido(boolean elegido) {
            this.elegido = elegido;
            setForeground(elegido ? Color.WHITE : Estilo.TEXTO);
            setFont(Estilo.fuente(elegido ? Font.BOLD : Font.PLAIN, 14f));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Estilo.suave(g);
            if (!isEnabled() && !elegido) {
                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.5f));
            }
            int arco = getHeight();
            g2.setColor(elegido ? Estilo.CAFE : Estilo.BLANCO);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
            if (!elegido) {
                g2.setColor(Estilo.BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
