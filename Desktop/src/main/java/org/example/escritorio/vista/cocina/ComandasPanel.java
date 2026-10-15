package org.example.escritorio.vista.cocina;

import org.example.Modelo.enums.OrigenTicket;
import org.example.dto.ComandaDTO;
import org.example.escritorio.controlador.cocina.ComandasControlador;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.comun.Icono;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.font.TextAttribute;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Comandas para la cocina (RF-23, RF-24): los tickets abiertos que tienen pedidos, de salón y de delivery.
 * A la izquierda la lista de comandas; a la derecha lo que hay que preparar de la elegida, sin precios.
 * Se actualiza sola cada 10 segundos mientras la pantalla está a la vista.
 *
 * Los platos se tildan a medida que salen y el botón de abajo guarda en la base solo los tildados:
 * esos dejan de aparecer y los que faltan siguen en la comanda. Con todos tildados, la comanda sale de la lista.
 */
public class ComandasPanel extends JPanel {

    /** Propiedad que avisa cuántas comandas hay (la usa la solapa para su contador). */
    public static final String PROP_CANTIDAD = "cantidadComandas";

    private static final int REFRESCO_MS = 10_000;

    private final DefaultListModel<ComandaDTO> modeloComandas = new DefaultListModel<>();
    private final JList<ComandaDTO> listaComandas = new JList<>(modeloComandas) {
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true; // las tarjetas ocupan todo el ancho de la columna
        }
    };

    private final JLabel lblCantidad = new JLabel(" ");
    private final JLabel lblEstado = new JLabel(" ");
    private final JButton btnActualizar = new Boton("Actualizar", new Icono(Icono.Tipo.ACTUALIZAR, 18, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);

    private final JLabel lblNumero = new JLabel(" ");
    private final JLabel lblTitulo = new JLabel("Comandas");
    private final JLabel lblDatos = new JLabel("Elegí una comanda para ver qué hay que preparar.");
    private final Estilo.Pildora lblOrigen = new Estilo.Pildora("", Estilo.AMARILLO_FONDO, Estilo.AMARILLO_TEXTO,
            Estilo.fuente(Font.BOLD, 15f), -1);
    private final JPanel panelPlatos = Estilo.transparente(null);
    private final JLabel lblListos = new JLabel(" ");
    private final JLabel lblListosDetalle = new JLabel(" ");
    private final JPanel pieDetalle = new JPanel(new BorderLayout());
    private final JButton btnComandaLista = new Boton("Comanda lista", new Icono(Icono.Tipo.TILDE, 18, Color.WHITE),
            Boton.Tipo.PRIMARIO);

    /** idPedido de los platos tildados y todavía no enviados. Son únicos, sirven para todas las comandas. */
    private final Set<Integer> pedidosTildados = new HashSet<>();
    private ComandaDTO comandaMostrada;
    /** true mientras se recarga la lista, para no limpiar el detalle por la selección que se pierde un instante. */
    private boolean recargandoLista;

    private final ComandasControlador controlador;
    private final Timer refresco;

    public ComandasPanel() {
        super(new BorderLayout(0, 22));
        setOpaque(false);
        setBorder(Estilo.relleno(4, 0, 0, 0));

        JPanel centro = Estilo.transparente(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridx = 0;
        c.weightx = 0.47;
        c.insets = new Insets(0, 0, 0, 14);
        // ancho preferido mínimo: así las dos columnas se reparten el lugar según weightx
        JComponent lista = armarLista();
        lista.setPreferredSize(new Dimension(100, 100));
        centro.add(lista, c);
        c.gridx = 1;
        c.weightx = 0.53;
        c.insets = new Insets(0, 14, 0, 0);
        JComponent detalle = armarDetalle();
        detalle.setPreferredSize(new Dimension(100, 100));
        centro.add(detalle, c);

        add(armarEstado(), BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);

        controlador = new ComandasControlador(this);

        listaComandas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !recargandoLista) {
                controlador.alCambiarSeleccion();
            }
        });
        btnActualizar.addActionListener(e -> controlador.cargar(true));
        btnComandaLista.addActionListener(e -> controlador.marcarLista());

        // solo refresca si la solapa está a la vista; el timer se apaga al cerrar la ventana
        refresco = new Timer(REFRESCO_MS, e -> {
            if (isShowing()) {
                controlador.cargar(false);
            }
        });

        limpiarDetalle();
        controlador.cargar(true);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        refresco.start();
    }

    @Override
    public void removeNotify() {
        refresco.stop();
        super.removeNotify();
    }

    // ---------- armado ----------

    private JComponent armarEstado() {
        lblCantidad.setFont(Estilo.fuente(Font.BOLD, 16f));
        lblCantidad.setForeground(Estilo.TEXTO);
        lblEstado.setFont(Estilo.fuente(Font.PLAIN, 16f));
        lblEstado.setForeground(Estilo.TEXTO_SUAVE);

        JPanel textos = Estilo.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        textos.add(lblCantidad);
        textos.add(lblEstado);
        JPanel textosCentrados = Estilo.transparente(new GridBagLayout());
        textosCentrados.add(textos);

        btnActualizar.setBorder(Estilo.relleno(11, 18, 11, 20));

        JPanel fila = Estilo.transparente(new BorderLayout());
        fila.add(textosCentrados, BorderLayout.WEST);
        fila.add(btnActualizar, BorderLayout.EAST);
        return fila;
    }

    private JComponent armarLista() {
        listaComandas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaComandas.setCellRenderer(new RendererComanda());
        listaComandas.setOpaque(false);
        listaComandas.setBackground(Estilo.FONDO);
        listaComandas.setFixedCellHeight(118);
        listaComandas.setFixedCellWidth(100);
        listaComandas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JScrollPane scroll = new JScrollPane(listaComandas);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private JComponent armarDetalle() {
        lblNumero.setFont(Estilo.conEspaciado(Estilo.fuente(Font.BOLD, 14f), 0.1f));
        lblNumero.setForeground(Estilo.CAFE);
        lblTitulo.setFont(Estilo.fuente(Font.BOLD, 26f));
        lblTitulo.setForeground(Estilo.TEXTO);
        lblDatos.setFont(Estilo.fuente(Font.PLAIN, 16f));
        lblDatos.setForeground(Estilo.TEXTO_SUAVE);
        lblOrigen.setBorder(Estilo.relleno(7, 16, 7, 16));

        JPanel textos = Estilo.transparente(null);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(lblNumero);
        textos.add(Box.createVerticalStrut(4));
        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(lblDatos);

        JPanel origen = Estilo.transparente(new BorderLayout());
        origen.add(lblOrigen, BorderLayout.NORTH);

        JPanel cabecera = Estilo.transparente(new BorderLayout(16, 0));
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),
                Estilo.relleno(26, 30, 24, 30)));
        cabecera.add(textos, BorderLayout.CENTER);
        cabecera.add(origen, BorderLayout.EAST);

        panelPlatos.setLayout(new BoxLayout(panelPlatos, BoxLayout.Y_AXIS));
        panelPlatos.setBorder(Estilo.relleno(8, 30, 8, 30));
        JPanel platosArriba = Estilo.transparente(new BorderLayout());
        platosArriba.add(panelPlatos, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(platosArriba);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        lblListos.setFont(Estilo.fuente(Font.BOLD, 16f));
        lblListos.setForeground(Estilo.TEXTO);
        lblListosDetalle.setFont(Estilo.fuente(Font.PLAIN, 16f));
        lblListosDetalle.setForeground(Estilo.TEXTO_SUAVE);
        JPanel listos = Estilo.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        listos.add(lblListos);
        listos.add(lblListosDetalle);
        pieDetalle.setBackground(Estilo.ENCABEZADO_TABLA);
        pieDetalle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDE),
                Estilo.relleno(22, 30, 22, 30)));
        JPanel listosCentrados = Estilo.transparente(new GridBagLayout());
        listosCentrados.add(listos);
        pieDetalle.add(listosCentrados, BorderLayout.WEST);
        pieDetalle.add(btnComandaLista, BorderLayout.EAST);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(cabecera, BorderLayout.NORTH);
        tarjeta.add(scroll, BorderLayout.CENTER);
        tarjeta.add(pieDetalle, BorderLayout.SOUTH);
        return tarjeta;
    }

    // ---------- lo que usa el controlador ----------

    /** Muestra las comandas y deja seleccionado el mismo ticket que estaba elegido, si sigue abierto. */
    public void mostrarComandas(List<ComandaDTO> comandas) {
        Integer elegido = comandaSeleccionada().map(ComandaDTO::nroTicket).orElse(null);
        List<ComandaDTO> ordenadas = comandas.stream()
                .sorted(Comparator.comparingInt(ComandaDTO::nroTicket))
                .toList();

        recargandoLista = true;
        try {
            modeloComandas.clear();
            ordenadas.forEach(modeloComandas::addElement);
            if (elegido != null) {
                for (int i = 0; i < modeloComandas.size(); i++) {
                    if (modeloComandas.get(i).nroTicket() == elegido) {
                        listaComandas.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } finally {
            recargandoLista = false;
        }
        // los tildes de pedidos que ya no están pendientes (se enviaron o se facturó el ticket) no sirven más
        Set<Integer> pendientes = new HashSet<>();
        ordenadas.forEach(cm -> cm.pedidos().forEach(item -> pendientes.add(item.idPedido())));
        pedidosTildados.retainAll(pendientes);

        lblCantidad.setText(comandas.size() + (comandas.size() == 1 ? " comanda" : " comandas"));
        lblEstado.setForeground(Estilo.TEXTO_SUAVE);
        lblEstado.setText(" · actualizado a las " + Formatos.hora(LocalTime.now()));
        firePropertyChange(PROP_CANTIDAD, -1, comandas.size());
    }

    public Optional<ComandaDTO> comandaSeleccionada() {
        return Optional.ofNullable(listaComandas.getSelectedValue());
    }

    public void mostrarDetalle(ComandaDTO comanda) {
        // el refresco automático vuelve a mostrar la misma comanda: si no cambió, no se redibuja
        if (comanda.equals(comandaMostrada)) {
            return;
        }
        comandaMostrada = comanda;

        lblNumero.setText("COMANDA #T-" + comanda.nroTicket());
        lblTitulo.setText(titulo(comanda));
        lblDatos.setText(datos(comanda));
        lblOrigen.setText(comanda.origen() == OrigenTicket.DELIVERY ? "Delivery" : "Salón");
        lblOrigen.setVisible(true);

        panelPlatos.removeAll();
        List<ComandaDTO.Item> items = comanda.pedidos();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                JPanel linea = new JPanel();
                linea.setBackground(Estilo.BORDE);
                linea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                linea.setPreferredSize(new Dimension(1, 1));
                linea.setAlignmentX(LEFT_ALIGNMENT);
                panelPlatos.add(linea);
            }
            panelPlatos.add(new FilaPlato(items.get(i)));
        }
        pieDetalle.setVisible(true);
        actualizarListos();
        panelPlatos.revalidate();
        panelPlatos.repaint();
    }

    public void limpiarDetalle() {
        comandaMostrada = null;
        lblNumero.setText("COMANDAS");
        lblTitulo.setText("Elegí una comanda");
        lblDatos.setText("A la izquierda están los tickets con pedidos para preparar.");
        lblOrigen.setVisible(false);
        panelPlatos.removeAll();
        pieDetalle.setVisible(false);
        panelPlatos.revalidate();
        panelPlatos.repaint();
    }

    /** Si falla el refresco automático no se abre un diálogo cada 10 segundos: se avisa arriba. */
    public void mostrarAviso(String mensaje) {
        lblEstado.setForeground(new Color(0xC62828));
        lblEstado.setText(" · " + mensaje);
    }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public JButton getBotonActualizar() { return btnActualizar; }
    public JButton getBotonComandaLista() { return btnComandaLista; }

    /** idPedido de los platos tildados en la comanda elegida (los que se van a marcar como listos). */
    public List<Integer> pedidosTildadosDeLaComanda() {
        if (comandaMostrada == null) {
            return List.of();
        }
        return comandaMostrada.pedidos().stream()
                .map(ComandaDTO.Item::idPedido)
                .filter(pedidosTildados::contains)
                .toList();
    }

    /** Los platos se guardaron como listos: se olvidan sus tildes y el detalle se vuelve a dibujar al recargar. */
    public void platosEnviados(List<Integer> idsPedido) {
        idsPedido.forEach(pedidosTildados::remove);
        comandaMostrada = null;
    }

    // ---------- textos ----------

    /** "#T-219 · Salón": la zona de la reserva o, si no hay, de dónde vino el ticket. */
    private static String encabezado(ComandaDTO c) {
        return "#T-" + c.nroTicket() + " · " + titulo(c);
    }

    private static String titulo(ComandaDTO c) {
        if (c.origen() == OrigenTicket.DELIVERY) {
            return "Delivery";
        }
        return c.zona() != null ? c.zona() : "Salón";
    }

    private static String datos(ComandaDTO c) {
        int platos = c.pedidos().stream().mapToInt(ComandaDTO.Item::cantidad).sum();
        StringBuilder sb = new StringBuilder();
        if (c.origen() == OrigenTicket.DELIVERY) {
            sb.append("Pedido de delivery");
        } else if (c.nroReserva() != null) {
            sb.append("Reserva #").append(c.nroReserva());
        } else {
            sb.append("Sin reserva");
        }
        if (c.comensales() != null) {
            sb.append(" · ").append(c.comensales()).append(" comensales");
        }
        sb.append(" · ").append(platos).append(platos == 1 ? " plato" : " platos");
        return sb.toString();
    }

    private void actualizarListos() {
        if (comandaMostrada == null) {
            return;
        }
        // se cuentan platos (cantidades), igual que en la lista: "2× Lasaña" son 2 platos
        int total = 0;
        int listos = 0;
        for (ComandaDTO.Item item : comandaMostrada.pedidos()) {
            total += item.cantidad();
            if (pedidosTildados.contains(item.idPedido())) {
                listos += item.cantidad();
            }
        }
        lblListos.setText(listos + " de " + total);
        lblListosDetalle.setText(total == 1 ? " plato listo" : " platos listos");

        // se envían solo los tildados: con todos es la comanda entera; sin ninguno no hay nada que enviar
        btnComandaLista.setEnabled(listos > 0);
        btnComandaLista.setText(listos == 0 || listos == total ? "Comanda lista"
                : "Enviar " + listos + (listos == 1 ? " plato listo" : " platos listos"));
    }

    // ---------- piezas visuales ----------

    /** Una línea de la comanda: tilde, cantidad, plato y observaciones. Tildado, el plato se tacha. */
    private class FilaPlato extends JPanel {
        FilaPlato(ComandaDTO.Item item) {
            super(new GridBagLayout());
            setOpaque(false);
            setBorder(Estilo.relleno(18, 0, 18, 0));
            setAlignmentX(LEFT_ALIGNMENT);

            JCheckBox tilde = new JCheckBox();
            tilde.setOpaque(false);
            tilde.setSelected(pedidosTildados.contains(item.idPedido()));
            tilde.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tilde.setToolTipText("Tildalo cuando el plato esté listo; se guarda con el botón de abajo");
            Estilo.casilla(tilde);

            Estilo.Pildora cantidad = new Estilo.Pildora(item.cantidad() + "×", Estilo.SEGMENTO_FONDO,
                    Estilo.CAFE, Estilo.fuente(Font.BOLD, 16f), 12);
            cantidad.setBorder(Estilo.relleno(8, 14, 8, 14));

            JLabel plato = new JLabel(item.plato());
            Font normal = Estilo.fuente(Font.BOLD, 18f);
            Font tachada = normal.deriveFont(Map.of(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON));

            JComponent observacion;
            if (item.observaciones() == null || item.observaciones().isBlank()) {
                JLabel sin = new JLabel("Sin observaciones");
                sin.setFont(Estilo.fuente(Font.PLAIN, 15f));
                sin.setForeground(Estilo.TEXTO_SUAVE);
                observacion = sin;
            } else {
                Estilo.Pildora obs = new Estilo.Pildora(item.observaciones(), Estilo.AMARILLO_FONDO,
                        Estilo.AMARILLO_TEXTO, Estilo.fuente(Font.BOLD, 15f), 10);
                obs.setBorder(Estilo.relleno(5, 12, 5, 12));
                observacion = obs;
            }

            Runnable pintar = () -> {
                boolean listo = tilde.isSelected();
                plato.setFont(listo ? tachada : normal);
                plato.setForeground(listo ? Estilo.TEXTO_SUAVE : Estilo.TEXTO);
            };
            pintar.run();
            tilde.addActionListener(e -> {
                if (tilde.isSelected()) {
                    pedidosTildados.add(item.idPedido());
                } else {
                    pedidosTildados.remove(item.idPedido());
                }
                pintar.run();
                actualizarListos();
            });

            JPanel textos = Estilo.transparente(null);
            textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
            plato.setAlignmentX(LEFT_ALIGNMENT);
            observacion.setAlignmentX(LEFT_ALIGNMENT);
            textos.add(plato);
            textos.add(Box.createVerticalStrut(6));
            textos.add(observacion);

            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.insets = new Insets(4, 0, 0, 18);
            add(tilde, c);
            c.insets = new Insets(0, 0, 0, 18);
            add(cantidad, c);
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(4, 0, 0, 0);
            add(textos, c);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }

    /** Cada comanda de la lista es una tarjeta: título, datos y una rayita de color a la izquierda. */
    private static class RendererComanda extends JPanel implements ListCellRenderer<ComandaDTO> {
        private final JLabel titulo = new JLabel();
        private final JLabel datos = new JLabel();
        private boolean elegida;
        private Color acento = Estilo.CAFE;

        RendererComanda() {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(Estilo.relleno(0, 0, 16, 0));

            titulo.setFont(Estilo.fuente(Font.BOLD, 20f));
            titulo.setForeground(Estilo.TEXTO);
            // margen para que la negrita no quede recortada en la última letra
            titulo.setBorder(Estilo.relleno(0, 0, 0, 4));
            datos.setFont(Estilo.fuente(Font.PLAIN, 16f));
            datos.setForeground(Estilo.TEXTO_SUAVE);

            JPanel textos = Estilo.transparente(null);
            textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
            textos.setBorder(Estilo.relleno(0, 26, 0, 22));
            textos.add(Box.createVerticalGlue());
            textos.add(titulo);
            textos.add(Box.createVerticalStrut(6));
            textos.add(datos);
            textos.add(Box.createVerticalGlue());
            add(textos, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ComandaDTO> lista, ComandaDTO c, int indice,
                                                      boolean seleccionada, boolean foco) {
            titulo.setText(encabezado(c));
            datos.setText(datosCortos(c));
            elegida = seleccionada;
            acento = c.origen() == OrigenTicket.DELIVERY ? Estilo.AMARILLO_TEXTO : Estilo.CAFE;
            return this;
        }

        private static String datosCortos(ComandaDTO c) {
            int platos = c.pedidos().stream().mapToInt(ComandaDTO.Item::cantidad).sum();
            String origen = c.origen() == OrigenTicket.DELIVERY ? "Pedido de delivery"
                    : c.nroReserva() != null ? "Reserva #" + c.nroReserva() : "Sin reserva";
            String comensales = c.comensales() != null ? " · " + c.comensales() + " comensales" : "";
            return origen + comensales + " · " + platos + (platos == 1 ? " plato" : " platos");
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Estilo.suave(g);
            int alto = getHeight() - getInsets().bottom - 1;
            int ancho = getWidth() - 1;
            g2.setColor(elegida ? Estilo.FILA_SELECCIONADA : Estilo.BLANCO);
            g2.fillRoundRect(0, 0, ancho, alto, 20, 20);
            // rayita de color a la izquierda, recortada a la forma de la tarjeta
            g2.clip(new java.awt.geom.RoundRectangle2D.Float(0, 0, ancho, alto, 20, 20));
            g2.setColor(acento);
            g2.fillRect(0, 0, 5, alto);
            g2.setClip(null);
            g2.setColor(elegida ? Estilo.CAFE : Estilo.BORDE);
            g2.setStroke(new java.awt.BasicStroke(elegida ? 2f : 1f));
            g2.drawRoundRect(elegida ? 1 : 0, elegida ? 1 : 0, ancho - (elegida ? 2 : 0), alto - (elegida ? 2 : 0), 20, 20);
            g2.dispose();
        }
    }
}
