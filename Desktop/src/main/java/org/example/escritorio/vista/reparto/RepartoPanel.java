package org.example.escritorio.vista.reparto;

import org.example.Modelo.enums.EstadoEnvio;
import org.example.dto.RemitoDTO;
import org.example.escritorio.controlador.reparto.RepartoControlador;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.caja.CobrosPanel;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formulario;
import org.example.escritorio.vista.comun.Icono;
import org.example.escritorio.vista.comun.Selector;
import org.example.escritorio.vista.comun.Tablas;

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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Reparto (RF-36 a RF-39): los envíos del repartidor logueado (el ADMIN ve todos),
 * con los cambios de estado y el remito. Asignar repartidor es solo para ADMIN (RF-36).
 * A la izquierda la lista; a la derecha el detalle del envío elegido con sus acciones.
 */
public class RepartoPanel extends JPanel {

    private static final int ALTO_FILA = 62;
    private static final String SIN_ASIGNAR = "Sin asignar";

    private final EnvioTableModel modelo = new EnvioTableModel();
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<EnvioTableModel> orden = new TableRowSorter<>(modelo);

    private final Selector selFiltro = new Selector("Activos", "Pendientes", "En camino", "Todos");
    private final JButton btnActualizar = new Boton(null, new Icono(Icono.Tipo.ACTUALIZAR, 20, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);
    private final JLabel lblResumen = new JLabel(" ");
    private final JLabel lblResumenDetalle = new JLabel(" ");

    // detalle del envío elegido
    private final JLabel lblNumero = new JLabel(" ");
    private final JLabel lblNombre = new JLabel(" ");
    private final JLabel lblDireccion = new JLabel(" ", new Icono(Icono.Tipo.UBICACION, 18, Estilo.TEXTO_SUAVE), JLabel.LEFT);
    private final JLabel lblCelular = new JLabel(" ", new Icono(Icono.Tipo.TELEFONO, 18, Estilo.TEXTO_SUAVE), JLabel.LEFT);
    private final Pasos pasos = new Pasos();
    private final Estilo.Pildora lblCancelado = new Estilo.Pildora("Envío cancelado", Estilo.GRIS_FONDO,
            Estilo.GRIS_TEXTO, Estilo.fuente(Font.BOLD, 14f), -1);
    private final JPanel panelPlatos = Estilo.transparente(null);
    private final JLabel lblPago = new JLabel(" ");
    private final JLabel lblTotal = new JLabel(" ");
    private final JLabel lblRepartidor = new JLabel(" ");
    private final JPanel cuerpoDetalle = Estilo.transparente(new BorderLayout());

    private final JButton btnAsignar = new Boton("Asignar", Boton.Tipo.CONTORNO);
    private final JButton btnRemito = new Boton("Remito", Boton.Tipo.CONTORNO);
    private final JButton btnCancelar = new Boton("Cancelar", Boton.Tipo.CONTORNO_CAFE);
    private final JButton btnEnCamino = new Boton("Salió · en camino", new Icono(Icono.Tipo.BICI, 18, Color.WHITE),
            Boton.Tipo.PRIMARIO);
    private final JButton btnEntregado = new Boton("Entregado", new Icono(Icono.Tipo.TILDE, 18, Color.WHITE),
            Boton.Tipo.PRIMARIO);

    private final RepartoControlador controlador;

    public RepartoPanel() {
        super(new BorderLayout(0, 22));
        setBackground(Estilo.FONDO);
        setBorder(Estilo.relleno(30, 48, 28, 48));

        add(armarEncabezado(), BorderLayout.NORTH);
        add(armarCentro(), BorderLayout.CENTER);
        add(armarResumen(), BorderLayout.SOUTH);

        controlador = new RepartoControlador(this);

        selFiltro.alElegir(i -> aplicarFiltro());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle(envioSeleccionado().orElse(null));
                controlador.alCambiarSeleccion();
            }
        });
        btnActualizar.addActionListener(e -> controlador.cargar(null));
        btnAsignar.addActionListener(e -> controlador.asignarRepartidor());
        btnRemito.addActionListener(e -> controlador.verRemito());
        btnEnCamino.addActionListener(e -> controlador.cambiarEstado(EstadoEnvio.EN_CAMINO));
        btnEntregado.addActionListener(e -> controlador.cambiarEstado(EstadoEnvio.ENTREGADO));
        btnCancelar.addActionListener(e -> controlador.cambiarEstado(EstadoEnvio.CANCELADO));

        mostrarDetalle(null);
        habilitarAcciones(false, false, false, false, false);
        controlador.cargar(null);
    }

    // ---------- armado ----------

    private JComponent armarEncabezado() {
        btnActualizar.setToolTipText("Actualizar");
        btnActualizar.setBorder(Estilo.relleno(12, 12, 12, 12));
        JPanel derecha = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(selFiltro);
        derecha.add(Box.createHorizontalStrut(14));
        derecha.add(btnActualizar);
        JPanel derechaCentrada = Estilo.transparente(new GridBagLayout());
        derechaCentrada.add(derecha);

        JPanel encabezado = Estilo.transparente(new BorderLayout());
        encabezado.add(Estilo.tituloPantalla("Reparto", Sesion.esAdmin() ? "Envíos" : "Mis envíos"),
                BorderLayout.CENTER);
        encabezado.add(derechaCentrada, BorderLayout.EAST);
        return encabezado;
    }

    private JComponent armarCentro() {
        JComponent lista = armarTabla();
        JComponent detalle = armarDetalle();
        // ancho preferido mínimo: así las dos columnas se reparten el lugar según weightx
        lista.setPreferredSize(new Dimension(100, 100));
        detalle.setPreferredSize(new Dimension(100, 100));

        JPanel centro = Estilo.transparente(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.weightx = 0.6;
        c.insets = new Insets(0, 0, 0, 14);
        centro.add(lista, c);
        c.gridx = 1;
        c.weightx = 0.4;
        c.insets = new Insets(0, 14, 0, 0);
        centro.add(detalle, c);
        return centro;
    }

    private JComponent armarTabla() {
        orden.setSortKeys(List.of(new RowSorter.SortKey(EnvioTableModel.COL_ENVIO, SortOrder.ASCENDING)));
        tabla.setRowSorter(orden);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Tablas.estilizar(tabla, ALTO_FILA);

        Font normal = Estilo.fuente(Font.PLAIN, 16f);
        Font negrita = Estilo.fuente(Font.BOLD, 16f);
        Tablas.renderer(tabla, 0, new RendererEnvio(normal));
        Tablas.renderer(tabla, 2, new Tablas.RendererTexto(normal, Estilo.TEXTO, String::valueOf));
        Tablas.renderer(tabla, 3, new Tablas.RendererTexto(normal, Estilo.TEXTO, String::valueOf));
        Tablas.renderer(tabla, 5, new Tablas.RendererTexto(negrita, Estilo.TEXTO,
                v -> Tablas.pesosCortos((BigDecimal) v)));
        Tablas.renderer(tabla, EnvioTableModel.COL_ESTADO,
                new Tablas.RendererInsignia(v -> insignia((EstadoEnvio) v)));
        Tablas.renderer(tabla, 8, new RendererRepartidor(normal));
        Tablas.anchos(tabla, 78, 10, 110, 160, 10, 95, 10, 125, 124);
        // lo que no entra en la fila se ve en el detalle: ticket (va debajo del envío), platos y pago
        tabla.removeColumn(tabla.getColumnModel().getColumn(6));
        tabla.removeColumn(tabla.getColumnModel().getColumn(4));
        tabla.removeColumn(tabla.getColumnModel().getColumn(1));

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(Tablas.scroll(tabla), BorderLayout.CENTER);
        return tarjeta;
    }

    private JComponent armarDetalle() {
        lblNumero.setFont(Estilo.conEspaciado(Estilo.fuente(Font.BOLD, 14f), 0.1f));
        lblNumero.setForeground(Estilo.CAFE);
        lblNumero.setBorder(Estilo.relleno(0, 0, 0, 8));
        lblNombre.setFont(Estilo.fuente(Font.BOLD, 26f));
        lblNombre.setForeground(Estilo.TEXTO);
        lblNombre.setBorder(Estilo.relleno(0, 0, 0, 6));
        for (JLabel l : new JLabel[]{lblDireccion, lblCelular}) {
            l.setFont(Estilo.fuente(Font.PLAIN, 16f));
            l.setForeground(Estilo.TEXTO);
            l.setIconTextGap(10);
        }

        JPanel cabecera = Estilo.transparente(null);
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                new Estilo.LineaPunteada(false), Estilo.relleno(24, 28, 20, 28)));
        cabecera.add(lblNumero);
        cabecera.add(Box.createVerticalStrut(6));
        cabecera.add(lblNombre);
        cabecera.add(Box.createVerticalStrut(8));
        cabecera.add(lblDireccion);
        cabecera.add(Box.createVerticalStrut(4));
        cabecera.add(lblCelular);

        // pasos del envío (o la etiqueta de cancelado), platos, pago y repartidor
        lblCancelado.setBorder(Estilo.relleno(6, 14, 6, 14));
        JPanel estado = Estilo.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        estado.add(pasos);
        estado.add(lblCancelado);

        panelPlatos.setLayout(new BoxLayout(panelPlatos, BoxLayout.Y_AXIS));

        JLabel tituloPago = new JLabel("Pago");
        tituloPago.setFont(Estilo.fuente(Font.PLAIN, 15f));
        tituloPago.setForeground(Estilo.TEXTO_SUAVE);
        lblPago.setFont(Estilo.fuente(Font.BOLD, 16f));
        lblPago.setForeground(Estilo.TEXTO);
        JPanel textosPago = Estilo.transparente(new GridLayout(2, 1, 0, 2));
        textosPago.add(tituloPago);
        textosPago.add(lblPago);
        lblTotal.setFont(Estilo.fuente(Font.BOLD, 28f));
        lblTotal.setForeground(Estilo.CAFE_OSCURO);
        lblTotal.setBorder(Estilo.relleno(0, 0, 0, 6));
        Estilo.Tarjeta pago = new Estilo.Tarjeta(new BorderLayout(12, 0), 16);
        pago.setBackground(Estilo.FILA_SELECCIONADA);
        pago.setBorder(Estilo.relleno(14, 20, 14, 20));
        pago.add(textosPago, BorderLayout.CENTER);
        pago.add(lblTotal, BorderLayout.EAST);

        lblRepartidor.setFont(Estilo.fuente(Font.BOLD, 17f));
        btnAsignar.setBorder(Estilo.relleno(8, 16, 8, 16));
        JPanel filaRepartidor = Estilo.transparente(new BorderLayout(12, 0));
        filaRepartidor.add(lblRepartidor, BorderLayout.CENTER);
        if (Sesion.esAdmin()) {
            filaRepartidor.add(btnAsignar, BorderLayout.EAST);
        }

        JPanel contenido = Formulario.cuerpo(
                estado,
                panelPlatos,
                pago,
                Formulario.campo("Repartidor", filaRepartidor, null));
        contenido.setBorder(Estilo.relleno(18, 28, 18, 28));
        JPanel contenidoArriba = Estilo.transparente(new BorderLayout());
        contenidoArriba.add(contenido, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(contenidoArriba);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // abajo los botones: Remito y Cancelar, y la acción que sigue (salió o entregado)
        btnEnCamino.setFont(Estilo.fuente(Font.BOLD, 16f));
        btnEntregado.setFont(Estilo.fuente(Font.BOLD, 16f));
        btnEnCamino.setBorder(Estilo.relleno(14, 20, 14, 22));
        btnEntregado.setBorder(Estilo.relleno(14, 20, 14, 22));
        // las dos acciones ocupan el mismo lugar: se ve una sola (la que sigue)
        JPanel principal = Estilo.transparente(new BorderLayout());
        principal.add(btnEnCamino, BorderLayout.CENTER);
        principal.add(btnEntregado, BorderLayout.NORTH);
        JPanel secundarios = Estilo.transparente(new GridLayout(1, 2, 10, 0));
        secundarios.add(btnRemito);
        secundarios.add(btnCancelar);
        JPanel botones = Estilo.transparente(new BorderLayout(0, 10));
        botones.setBorder(Estilo.relleno(14, 28, 24, 28));
        botones.add(secundarios, BorderLayout.NORTH);
        botones.add(principal, BorderLayout.CENTER);

        cuerpoDetalle.add(scroll, BorderLayout.CENTER);
        cuerpoDetalle.add(botones, BorderLayout.SOUTH);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(cabecera, BorderLayout.NORTH);
        tarjeta.add(cuerpoDetalle, BorderLayout.CENTER);
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

    // ---------- detalle ----------

    private void mostrarDetalle(RemitoDTO r) {
        if (r == null) {
            lblNumero.setText("REPARTO");
            lblNombre.setText("Elegí un envío");
            lblDireccion.setText("Acá vas a ver la dirección, los platos y el pago.");
            lblDireccion.setIcon(null);
            lblCelular.setVisible(false);
            cuerpoDetalle.setVisible(false);
            return;
        }
        cuerpoDetalle.setVisible(true);
        lblNumero.setText("ENVÍO #E-" + r.idEnvio() + " · TICKET " + r.nroTicket());
        lblNombre.setText(r.nombreReceptor() != null ? r.nombreReceptor() : r.cliente());
        lblDireccion.setText(r.direccionEntrega());
        lblDireccion.setIcon(new Icono(Icono.Tipo.UBICACION, 18, Estilo.TEXTO_SUAVE));
        lblCelular.setText(r.celular() == null || r.celular().isBlank() ? "Sin celular" : r.celular());
        lblCelular.setVisible(true);

        boolean cancelado = r.estado() == EstadoEnvio.CANCELADO;
        pasos.setVisible(!cancelado);
        lblCancelado.setVisible(cancelado);
        pasos.setPaso(switch (r.estado()) {
            case PENDIENTE -> 0;
            case EN_CAMINO -> 1;
            default -> 2;
        });

        panelPlatos.removeAll();
        List<RemitoDTO.Item> items = r.pedidos();
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
        panelPlatos.revalidate();

        String metodo = r.metodoPago() == null ? "Sin método" : CobrosPanel.texto(r.metodoPago());
        lblPago.setText("PAGADO".equals(r.estadoPago()) ? metodo + " · ya cobrado" : metodo + " · cobrar al entregar");
        lblTotal.setText(Tablas.pesosCortos(r.total()));
        boolean sinRepartidor = SIN_ASIGNAR.equals(r.repartidor());
        lblRepartidor.setText(r.repartidor());
        lblRepartidor.setForeground(sinRepartidor ? Estilo.ROJO_TEXTO : Estilo.TEXTO);
        btnAsignar.setText(sinRepartidor ? "Asignar" : "Cambiar");
    }

    // ---------- lo que usa el controlador ----------

    public void mostrarEnvios(List<RemitoDTO> envios) {
        modelo.setFilas(envios);
        aplicarFiltro();
    }

    public Optional<RemitoDTO> envioSeleccionado() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return Optional.empty();
        }
        return Optional.of(modelo.getFila(tabla.convertRowIndexToModel(filaVista)));
    }

    public void seleccionarEnvio(int idEnvio) {
        for (int fila = 0; fila < tabla.getRowCount(); fila++) {
            if (modelo.getFila(tabla.convertRowIndexToModel(fila)).idEnvio() == idEnvio) {
                tabla.setRowSelectionInterval(fila, fila);
                mostrarDetalle(envioSeleccionado().orElse(null)); // los datos pueden haber cambiado
                return;
            }
        }
    }

    /** La acción que sigue es una sola: si se puede marcar entregado se muestra esa; si no, "Salió". */
    public void habilitarAcciones(boolean asignar, boolean remito, boolean enCamino,
                                  boolean entregado, boolean cancelar) {
        btnAsignar.setEnabled(asignar);
        btnRemito.setEnabled(remito);
        btnEnCamino.setEnabled(enCamino);
        btnEntregado.setEnabled(entregado);
        btnCancelar.setEnabled(cancelar);
        btnEntregado.setVisible(entregado);
        btnEnCamino.setVisible(!entregado);
    }

    public JButton getBotonActualizar() { return btnActualizar; }
    public JButton getBotonRemito() { return btnRemito; }

    public JButton botonPara(EstadoEnvio estado) {
        return switch (estado) {
            case EN_CAMINO -> btnEnCamino;
            case ENTREGADO -> btnEntregado;
            default -> btnCancelar;
        };
    }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public boolean confirmar(String pregunta) {
        return Dialogos.confirmar(this, pregunta);
    }

    // ---------- filtro ----------

    private void aplicarFiltro() {
        int filtro = selFiltro.getElegida();
        orden.setRowFilter(filtro == 3 ? null : new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends EnvioTableModel, ? extends Integer> fila) {
                EstadoEnvio e = fila.getModel().getFila(fila.getIdentifier()).estado();
                return switch (filtro) {
                    case 1 -> e == EstadoEnvio.PENDIENTE;
                    case 2 -> e == EstadoEnvio.EN_CAMINO;
                    default -> e == EstadoEnvio.PENDIENTE || e == EstadoEnvio.EN_CAMINO;
                };
            }
        });

        int pendientes = 0;
        int enCamino = 0;
        int sinRepartidor = 0;
        for (int i = 0; i < modelo.getRowCount(); i++) {
            RemitoDTO r = modelo.getFila(i);
            if (r.estado() == EstadoEnvio.PENDIENTE) {
                pendientes++;
            } else if (r.estado() == EstadoEnvio.EN_CAMINO) {
                enCamino++;
            }
            boolean activo = r.estado() == EstadoEnvio.PENDIENTE || r.estado() == EstadoEnvio.EN_CAMINO;
            if (activo && SIN_ASIGNAR.equals(r.repartidor())) {
                sinRepartidor++;
            }
        }
        selFiltro.setTexto(0, "Activos · " + (pendientes + enCamino));
        selFiltro.setTexto(1, "Pendientes · " + pendientes);
        selFiltro.setTexto(2, "En camino · " + enCamino);
        lblResumen.setText(pendientes + (pendientes == 1 ? " pendiente" : " pendientes"));
        lblResumenDetalle.setText(" · " + enCamino + " en camino · " + sinRepartidor + " sin repartidor");
    }

    /** Pendiente amarillo, en camino azul, entregado verde, cancelado gris. */
    private static Tablas.Insignia insignia(EstadoEnvio estado) {
        String texto = EnvioTableModel.texto(estado);
        return switch (estado) {
            case PENDIENTE -> Tablas.Insignia.amarilla(texto);
            case EN_CAMINO -> Tablas.Insignia.azul(texto);
            case ENTREGADO -> Tablas.Insignia.verde(texto);
            case CANCELADO -> Tablas.Insignia.gris(texto);
        };
    }

    // ---------- piezas visuales ----------

    /** "#E-31" y debajo, en gris, "Tk 14". */
    private class RendererEnvio extends Tablas.RendererTexto {
        RendererEnvio(Font fuente) {
            super(fuente, Estilo.TEXTO_SUAVE, String::valueOf);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(t, valor, seleccionada, foco, fila, columna);
            RemitoDTO r = modelo.getFila(t.convertRowIndexToModel(fila));
            setText("<html><center>#E-" + r.idEnvio() + "<br><font size='-1'>Tk " + r.nroTicket() + "</font></center></html>");
            return this;
        }
    }

    /** El repartidor; "Sin asignar" en rojo. */
    private static class RendererRepartidor extends Tablas.RendererTexto {
        RendererRepartidor(Font fuente) {
            super(fuente, Estilo.TEXTO, String::valueOf);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(t, valor, seleccionada, foco, fila, columna);
            if (SIN_ASIGNAR.equals(valor)) {
                setForeground(Estilo.ROJO_TEXTO);
            }
            return this;
        }
    }

    /** "2×  Lasaña a la boloñesa" y, si hay, la observación en gris. */
    private static class FilaPlato extends JPanel {
        FilaPlato(RemitoDTO.Item item) {
            super(new BorderLayout(14, 0));
            setOpaque(false);
            setBorder(Estilo.relleno(10, 0, 10, 0));
            setAlignmentX(LEFT_ALIGNMENT);

            JLabel cantidad = new JLabel(item.cantidad() + "×");
            cantidad.setFont(Estilo.fuente(Font.BOLD, 16f));
            cantidad.setForeground(Estilo.CAFE);
            cantidad.setPreferredSize(new Dimension(36, cantidad.getPreferredSize().height));

            String texto = item.plato();
            if (item.observaciones() != null && !item.observaciones().isBlank()) {
                texto = "<html>" + item.plato() + "<br><font color='#7D746C'>" + item.observaciones() + "</font></html>";
            }
            JLabel plato = new JLabel(texto);
            plato.setFont(Estilo.fuente(Font.PLAIN, 16f));
            plato.setForeground(Estilo.TEXTO);

            add(cantidad, BorderLayout.WEST);
            add(plato, BorderLayout.CENTER);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }

    /** ① Pendiente — ② En camino — ③ Entregado, con los pasos hechos en café. */
    private static class Pasos extends JComponent {
        private static final String[] NOMBRES = {"Pendiente", "En camino", "Entregado"};
        private static final int DIAMETRO = 26;
        private static final int RAYA = 12;
        private int paso;

        Pasos() {
            setFont(Estilo.fuente(Font.BOLD, 14f));
        }

        void setPaso(int paso) {
            this.paso = paso;
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int ancho = 0;
            for (String n : NOMBRES) {
                ancho += DIAMETRO + 8 + fm.stringWidth(n);
            }
            ancho += 2 * (RAYA + 20);
            return new Dimension(ancho, DIAMETRO + 4);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Estilo.suave(g);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = 0;
            int y = (getHeight() - DIAMETRO) / 2;
            for (int i = 0; i < NOMBRES.length; i++) {
                boolean hecho = i <= paso;
                if (hecho) {
                    g2.setColor(Estilo.CAFE);
                    g2.fillOval(x, y, DIAMETRO, DIAMETRO);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(Estilo.BORDE);
                    g2.drawOval(x, y, DIAMETRO - 1, DIAMETRO - 1);
                    g2.setColor(Estilo.TEXTO_SUAVE);
                }
                String numero = String.valueOf(i + 1);
                g2.drawString(numero, x + (DIAMETRO - fm.stringWidth(numero)) / 2,
                        y + (DIAMETRO - fm.getHeight()) / 2 + fm.getAscent());
                x += DIAMETRO + 8;
                g2.setColor(hecho ? Estilo.CAFE : Estilo.TEXTO_SUAVE);
                g2.drawString(NOMBRES[i], x, y + (DIAMETRO - fm.getHeight()) / 2 + fm.getAscent());
                x += fm.stringWidth(NOMBRES[i]) + 10;
                if (i < NOMBRES.length - 1) {
                    g2.setColor(i < paso ? Estilo.CAFE : Estilo.BORDE);
                    g2.fillRect(x, y + DIAMETRO / 2 - 1, RAYA, 2);
                    x += RAYA + 10;
                }
            }
            g2.dispose();
        }
    }
}
