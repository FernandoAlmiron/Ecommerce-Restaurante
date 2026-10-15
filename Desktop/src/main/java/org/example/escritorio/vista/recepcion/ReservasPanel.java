package org.example.escritorio.vista.recepcion;

import org.example.Modelo.enums.EstadoReserva;
import org.example.dto.ReservaResponseDTO;
import org.example.escritorio.controlador.recepcion.ReservasControlador;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.comun.Icono;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

/**
 * Pantalla de Recepción: lista de reservas con filtros y las acciones de cada reserva.
 * Solo dibuja; lo que pasa al hacer clic lo decide ReservasControlador.
 */
public class ReservasPanel extends JPanel {

    private static final Locale ARGENTINA = Locale.forLanguageTag("es-AR");
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", ARGENTINA);
    private static final int ALTO_FILA = 58;
    private static final int RELLENO_CELDA = 24;

    private final ReservaTableModel modelo = new ReservaTableModel();
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<ReservaTableModel> orden = new TableRowSorter<>(modelo);
    private TableColumn columnaFecha;

    /** Estado elegido en el selector; null = todas. */
    private EstadoReserva filtroEstado;
    private final JCheckBox chkSoloHoy = new JCheckBox("Solo hoy", true);
    private final JTextField txtBuscar = new JTextField(14);
    private final JButton btnActualizar = new Boton(null, new Icono(Icono.Tipo.ACTUALIZAR, 20, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);
    private final JButton btnWalkIn = new Boton("Nuevo walk-in", new Icono(Icono.Tipo.MAS, 16, Color.WHITE),
            Boton.Tipo.PRIMARIO);

    private final JButton btnConfirmar = new Boton("Confirmar", Boton.Tipo.OSCURO);
    private final JButton btnCancelar = new Boton("Cancelar reserva", Boton.Tipo.CONTORNO_CAFE);
    private final JButton btnAsistio = new Boton("Asistió", Boton.Tipo.CONTORNO);
    private final JButton btnNoAsistio = new Boton("No asistió", Boton.Tipo.CONTORNO);
    private final JLabel lblResumen = new JLabel(" ");
    private final JLabel lblResumenDetalle = new JLabel(" ");

    private final ReservasControlador controlador;

    public ReservasPanel() {
        super(new BorderLayout(0, 24));
        setBackground(Estilo.FONDO);
        setBorder(Estilo.relleno(30, 48, 28, 48));

        add(armarEncabezado(), BorderLayout.NORTH);
        add(armarTabla(), BorderLayout.CENTER);
        add(armarAcciones(), BorderLayout.SOUTH);

        controlador = new ReservasControlador(this);

        // filtros: se aplican sobre lo que ya está cargado, sin volver a pedir datos
        chkSoloHoy.addActionListener(e -> aplicarFiltros());
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override public void removeUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override public void changedUpdate(DocumentEvent e) { aplicarFiltros(); }
        });

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                controlador.alCambiarSeleccion();
                actualizarResumen();
            }
        });
        btnActualizar.addActionListener(e -> controlador.cargar());
        btnWalkIn.addActionListener(e -> controlador.nuevoWalkIn());
        btnConfirmar.addActionListener(e -> controlador.confirmar());
        btnCancelar.addActionListener(e -> controlador.cancelar());
        btnAsistio.addActionListener(e -> controlador.marcarAsistencia(true));
        btnNoAsistio.addActionListener(e -> controlador.marcarAsistencia(false));

        habilitarAcciones(false, false, false);
        aplicarFiltros();
        controlador.cargar();
    }

    // ---------- armado ----------

    private JComponent armarEncabezado() {
        String dia = DIA.format(LocalDate.now());
        JLabel miga = new JLabel("Recepción · " + Character.toUpperCase(dia.charAt(0)) + dia.substring(1));
        miga.setFont(Estilo.fuente(Font.PLAIN, 15f));
        miga.setForeground(Estilo.TEXTO_SUAVE);

        JLabel titulo = new JLabel("Reservas");
        titulo.setFont(Estilo.fuente(Font.BOLD, 34f));
        titulo.setForeground(Estilo.TEXTO);

        JPanel textos = Estilo.transparente(null);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(miga);
        textos.add(Box.createVerticalStrut(4));
        textos.add(titulo);

        JPanel derecha = Estilo.transparente(new GridBagLayout());
        derecha.add(btnWalkIn);

        JPanel fila = Estilo.transparente(new BorderLayout());
        fila.add(textos, BorderLayout.CENTER);
        fila.add(derecha, BorderLayout.EAST);

        JPanel encabezado = Estilo.transparente(new BorderLayout(0, 24));
        encabezado.add(fila, BorderLayout.NORTH);
        encabezado.add(armarFiltros(), BorderLayout.CENTER);
        return encabezado;
    }

    /** Tarjeta con el buscador, el selector de estado, "Solo hoy" y el botón de actualizar. */
    private JComponent armarFiltros() {
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar cliente por nombre o teléfono");
        txtBuscar.putClientProperty("JTextField.leadingIcon", new Icono(Icono.Tipo.LUPA, 18, Estilo.TEXTO_SUAVE));
        txtBuscar.setFont(Estilo.fuente(Font.PLAIN, 15f));
        txtBuscar.setForeground(Estilo.TEXTO);
        Estilo.flat(txtBuscar, "arc: 14; margin: 10,12,10,12; focusWidth: 0; innerFocusWidth: 0;"
                + " borderColor: #E3DDD6; focusedBorderColor: #8C5E35");

        chkSoloHoy.setFont(Estilo.fuente(Font.PLAIN, 16f));
        chkSoloHoy.setForeground(Estilo.TEXTO);
        chkSoloHoy.setOpaque(false);
        chkSoloHoy.setIconTextGap(10);
        chkSoloHoy.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Estilo.flat(chkSoloHoy, "icon.selectedBackground: #8C5E35; icon.selectedBorderColor: #8C5E35;"
                + " icon.checkmarkColor: #FFFFFF; icon.focusWidth: 0");

        btnActualizar.setToolTipText("Actualizar");
        btnActualizar.setBorder(Estilo.relleno(12, 12, 12, 12));

        JPanel derecha = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(armarSelectorEstado());
        derecha.add(Box.createHorizontalStrut(22));
        derecha.add(chkSoloHoy);
        derecha.add(Box.createHorizontalStrut(18));
        derecha.add(btnActualizar);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(20, 0), 22);
        tarjeta.setBorder(Estilo.relleno(16, 18, 16, 18));
        tarjeta.add(txtBuscar, BorderLayout.CENTER);
        tarjeta.add(derecha, BorderLayout.EAST);
        return tarjeta;
    }

    /** Pestañas Todas / Pendientes / Confirmadas / Canceladas sobre un fondo gris claro. */
    private JComponent armarSelectorEstado() {
        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Estilo.suave(g);
                g2.setColor(Estilo.SEGMENTO_FONDO);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        selector.setOpaque(false);
        selector.setBorder(Estilo.relleno(4, 4, 4, 4));

        ButtonGroup grupo = new ButtonGroup();
        Object[][] opciones = {
                {"Todas", null},
                {"Pendientes", EstadoReserva.PENDIENTE},
                {"Confirmadas", EstadoReserva.CONFIRMADA},
                {"Canceladas", EstadoReserva.CANCELADA}};
        for (Object[] opcion : opciones) {
            Segmento s = new Segmento((String) opcion[0]);
            EstadoReserva estado = (EstadoReserva) opcion[1];
            s.addActionListener(e -> {
                filtroEstado = estado;
                aplicarFiltros();
            });
            grupo.add(s);
            selector.add(s);
            if (estado == null) {
                s.setSelected(true);
            }
        }
        return selector;
    }

    private JComponent armarTabla() {
        tabla.setRowSorter(orden);
        orden.setSortKeys(List.of(
                new RowSorter.SortKey(ReservaTableModel.COL_FECHA, SortOrder.ASCENDING),
                new RowSorter.SortKey(ReservaTableModel.COL_HORA, SortOrder.ASCENDING)));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(ALTO_FILA);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(Estilo.BORDE);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setBackground(Estilo.BLANCO);
        tabla.setForeground(Estilo.TEXTO);
        tabla.setSelectionBackground(Estilo.FILA_SELECCIONADA);
        tabla.setSelectionForeground(Estilo.TEXTO);
        tabla.setFont(Estilo.fuente(Font.PLAIN, 16f));
        tabla.setFocusable(false);
        Formatos.aplicarA(tabla);

        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setReorderingAllowed(false);
        encabezado.setResizingAllowed(false);
        encabezado.setDefaultRenderer(new RendererEncabezado());
        encabezado.setBackground(Estilo.ENCABEZADO_TABLA);

        Font normal = Estilo.fuente(Font.PLAIN, 16f);
        Font negrita = Estilo.fuente(Font.BOLD, 16f);
        renderer(0, normal, Estilo.TEXTO_SUAVE, v -> "#" + v);
        renderer(1, normal, Estilo.TEXTO, v -> Formatos.fecha((LocalDate) v));
        renderer(2, negrita, Estilo.TEXTO, v -> Formatos.hora((LocalTime) v));
        renderer(3, normal, Estilo.TEXTO, String::valueOf);
        renderer(4, normal, Estilo.TEXTO, String::valueOf);
        renderer(5, normal, Estilo.TEXTO, String::valueOf);
        tabla.getColumnModel().getColumn(6).setCellRenderer(new RendererEstado());
        tabla.getColumnModel().getColumn(7).setCellRenderer(new RendererSenia());
        renderer(8, normal, Estilo.TEXTO, String::valueOf);

        int[] anchos = {110, 120, 100, 220, 130, 140, 170, 140, 110};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        columnaFecha = tabla.getColumnModel().getColumn(ReservaTableModel.COL_FECHA);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        scroll.getViewport().setBackground(Estilo.BLANCO);
        scroll.setBackground(Estilo.BLANCO);
        scroll.setOpaque(false);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(scroll, BorderLayout.CENTER);

        // la tarjeta mide lo que ocupan las filas (como mucho, el alto disponible)
        JPanel contenedor = Estilo.transparente(new AjustarAlContenido(() ->
                encabezado.getPreferredSize().height + tabla.getRowCount() * ALTO_FILA + 3));
        contenedor.add(tarjeta);
        return contenedor;
    }

    private void renderer(int columna, Font fuente, Color color, Function<Object, String> texto) {
        tabla.getColumnModel().getColumn(columna).setCellRenderer(new RendererTexto(fuente, color, texto));
    }

    private JComponent armarAcciones() {
        JPanel botones = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        botones.add(btnNoAsistio);
        botones.add(btnAsistio);
        botones.add(btnCancelar);
        botones.add(btnConfirmar);

        lblResumen.setFont(Estilo.fuente(Font.BOLD, 16f));
        lblResumen.setForeground(Estilo.TEXTO);
        lblResumenDetalle.setFont(Estilo.fuente(Font.PLAIN, 16f));
        lblResumenDetalle.setForeground(Estilo.TEXTO_SUAVE);

        JPanel resumen = Estilo.transparente(new GridBagLayout()); // centrado en alto con los botones
        resumen.add(lblResumen);
        resumen.add(lblResumenDetalle);

        JPanel acciones = Estilo.transparente(new BorderLayout());
        acciones.add(resumen, BorderLayout.WEST);
        acciones.add(botones, BorderLayout.EAST);
        return acciones;
    }

    // ---------- lo que usa el controlador ----------

    public void mostrarReservas(List<ReservaResponseDTO> reservas) {
        modelo.setFilas(reservas);
        aplicarFiltros();
    }

    public void actualizarReserva(ReservaResponseDTO reserva) {
        modelo.reemplazar(r -> r.nroReserva() == reserva.nroReserva(), reserva);
        actualizarResumen();
    }

    public Optional<ReservaResponseDTO> reservaSeleccionada() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return Optional.empty();
        }
        return Optional.of(modelo.getFila(tabla.convertRowIndexToModel(filaVista)));
    }

    public void habilitarAcciones(boolean confirmar, boolean cancelar, boolean asistencia) {
        btnConfirmar.setEnabled(confirmar);
        btnCancelar.setEnabled(cancelar);
        btnAsistio.setEnabled(asistencia);
        btnNoAsistio.setEnabled(asistencia);
    }

    public JButton getBotonActualizar() { return btnActualizar; }
    public JButton getBotonConfirmar() { return btnConfirmar; }
    public JButton getBotonCancelar() { return btnCancelar; }
    public JButton getBotonAsistio() { return btnAsistio; }
    public JButton getBotonNoAsistio() { return btnNoAsistio; }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public void mostrarInfo(String mensaje) {
        Dialogos.info(this, mensaje);
    }

    public boolean confirmar(String pregunta) {
        return Dialogos.confirmar(this, pregunta);
    }

    // ---------- filtros ----------

    private void aplicarFiltros() {
        EstadoReserva estado = filtroEstado;
        boolean soloHoy = chkSoloHoy.isSelected();
        String texto = txtBuscar.getText().trim().toLowerCase();
        LocalDate hoy = LocalDate.now();

        orden.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends ReservaTableModel, ? extends Integer> fila) {
                ReservaResponseDTO r = fila.getModel().getFila(fila.getIdentifier());
                return (estado == null || r.estado() == estado)
                        && (!soloHoy || hoy.equals(r.fecha()))
                        && (texto.isEmpty() || r.cliente().toLowerCase().contains(texto));
            }
        });
        mostrarColumnaFecha(!soloHoy);
        actualizarResumen();
        revalidate();
        repaint();
    }

    /** Con "Solo hoy" todas las filas son del mismo día: la fecha sobra y se oculta. */
    private void mostrarColumnaFecha(boolean mostrar) {
        boolean visible = tabla.getColumnModel().getColumnCount() == modelo.getColumnCount();
        if (mostrar && !visible) {
            tabla.addColumn(columnaFecha);
            tabla.moveColumn(tabla.getColumnModel().getColumnCount() - 1, 1);
        } else if (!mostrar && visible) {
            tabla.removeColumn(columnaFecha);
        }
    }

    private void actualizarResumen() {
        int visibles = tabla.getRowCount();
        int total = modelo.getRowCount();
        int comensales = 0;
        for (int i = 0; i < visibles; i++) {
            comensales += modelo.getFila(tabla.convertRowIndexToModel(i)).comensales();
        }
        String cantidad = visibles == total
                ? total + (total == 1 ? " reserva" : " reservas")
                : visibles + " de " + total + " reservas";
        lblResumen.setText(cantidad);
        lblResumenDetalle.setText(" · " + comensales + " comensales"
                + (tabla.getSelectedRow() >= 0 ? " · 1 seleccionada" : ""));
    }

    /** $ 8.000 (sin centavos si no tiene). */
    private static String pesosCortos(BigDecimal monto) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(ARGENTINA);
        formato.setMinimumFractionDigits(monto.stripTrailingZeros().scale() > 0 ? 2 : 0);
        return formato.format(monto);
    }

    // ---------- piezas visuales ----------

    /** Pestaña del selector de estado: blanca y en negrita cuando está elegida. */
    private static class Segmento extends JToggleButton {
        Segmento(String texto) {
            super(texto);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(Estilo.relleno(10, 20, 10, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            putClientProperty("JButton.buttonType", "borderless");
            getModel().addChangeListener(e -> {
                setForeground(isSelected() ? Estilo.TEXTO : Estilo.TEXTO_SUAVE);
                setFont(Estilo.fuente(isSelected() ? Font.BOLD : Font.PLAIN, 16f));
            });
            setForeground(Estilo.TEXTO_SUAVE);
            setFont(Estilo.fuente(Font.PLAIN, 16f));
        }

        @Override
        public Dimension getPreferredSize() {
            // mismo ancho elegido o no (la negrita no tiene que mover a las demás)
            java.awt.FontMetrics fm = getFontMetrics(Estilo.fuente(Font.BOLD, 16f));
            Insets i = getInsets();
            return new Dimension(fm.stringWidth(getText()) + i.left + i.right + 2,
                    fm.getHeight() + i.top + i.bottom);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (isSelected()) {
                Graphics2D g2 = Estilo.suave(g);
                g2.setColor(new Color(0, 0, 0, 14));
                g2.fillRoundRect(0, 1, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 2, 12, 12);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    /** Títulos de columna: mayúsculas chiquitas, grises y con letras separadas. */
    private static class RendererEncabezado extends DefaultTableCellRenderer {
        RendererEncabezado() {
            setFont(Estilo.conEspaciado(Estilo.fuente(Font.BOLD, 13f), 0.08f));
            setForeground(Estilo.TEXTO_SUAVE);
            setBackground(Estilo.ENCABEZADO_TABLA);
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),
                    Estilo.relleno(20, RELLENO_CELDA, 20, 8)));
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            setText(valor == null ? "" : valor.toString().toUpperCase(ARGENTINA));
            return this;
        }
    }

    /** Celda de texto con relleno a la izquierda y su propia fuente y color. */
    private static class RendererTexto extends DefaultTableCellRenderer {
        private final Font fuente;
        private final Color color;
        private final Function<Object, String> texto;

        RendererTexto(Font fuente, Color color, Function<Object, String> texto) {
            this.fuente = fuente;
            this.color = color;
            this.texto = texto;
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, seleccionada, false, fila, columna);
            setText(valor == null ? "—" : texto.apply(valor));
            setFont(fuente);
            setForeground(color);
            setBorder(Estilo.relleno(0, RELLENO_CELDA, 0, 8));
            return this;
        }
    }

    /** Seña: el monto, o "Sin seña" en gris. En las canceladas el monto va en gris. */
    private static class RendererSenia extends RendererTexto {
        RendererSenia() {
            super(Estilo.fuente(Font.PLAIN, 16f), Estilo.TEXTO, v -> pesosCortos((BigDecimal) v));
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
            ReservaResponseDTO r = ((ReservaTableModel) tabla.getModel())
                    .getFila(tabla.convertRowIndexToModel(fila));
            if (valor == null) {
                setText("Sin seña");
            }
            if (valor == null || r.estado() == EstadoReserva.CANCELADA) {
                setForeground(Estilo.TEXTO_SUAVE);
            }
            return this;
        }
    }

    /** Estado como etiqueta redondeada: verde confirmada, amarilla pendiente, gris cancelada. */
    private static class RendererEstado implements TableCellRenderer {
        private final JPanel celda = new JPanel(new GridBagLayout());
        private final Etiqueta etiqueta = new Etiqueta();

        RendererEstado() {
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.weightx = 1;
            c.insets = new Insets(0, RELLENO_CELDA, 0, 0);
            celda.add(etiqueta, c);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            celda.setBackground(seleccionada ? tabla.getSelectionBackground() : tabla.getBackground());
            EstadoReserva estado = (EstadoReserva) valor;
            if (estado == null) {
                etiqueta.mostrar("", Estilo.BLANCO, Estilo.TEXTO);
            } else {
                switch (estado) {
                    case CONFIRMADA -> etiqueta.mostrar("Confirmada", Estilo.VERDE_FONDO, Estilo.VERDE_TEXTO);
                    case PENDIENTE -> etiqueta.mostrar("Pendiente", Estilo.AMARILLO_FONDO, Estilo.AMARILLO_TEXTO);
                    default -> etiqueta.mostrar("Cancelada", Estilo.GRIS_FONDO, Estilo.GRIS_TEXTO);
                }
            }
            return celda;
        }
    }

    private static class Etiqueta extends JLabel {
        private Color fondo = Estilo.GRIS_FONDO;

        Etiqueta() {
            setFont(Estilo.fuente(Font.BOLD, 14f));
            setBorder(Estilo.relleno(5, 14, 5, 14));
        }

        void mostrar(String texto, Color fondo, Color color) {
            setText(texto);
            this.fondo = fondo;
            setForeground(color);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Estilo.suave(g);
            g2.setColor(fondo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Ubica un único hijo arriba, con el alto que pide el contenido pero sin pasarse del lugar disponible
     * (si hay muchas filas, aparece la barra de desplazamiento de la tabla).
     */
    private static class AjustarAlContenido implements LayoutManager {
        private final java.util.function.IntSupplier altoDeseado;

        AjustarAlContenido(java.util.function.IntSupplier altoDeseado) {
            this.altoDeseado = altoDeseado;
        }

        @Override public void addLayoutComponent(String nombre, Component c) { }
        @Override public void removeLayoutComponent(Component c) { }

        @Override
        public Dimension preferredLayoutSize(Container padre) {
            return new Dimension(0, altoDeseado.getAsInt());
        }

        @Override
        public Dimension minimumLayoutSize(Container padre) {
            return new Dimension(0, 0);
        }

        @Override
        public void layoutContainer(Container padre) {
            if (padre.getComponentCount() == 0) {
                return;
            }
            int alto = Math.min(altoDeseado.getAsInt(), padre.getHeight());
            padre.getComponent(0).setBounds(0, 0, padre.getWidth(), alto);
        }
    }
}
