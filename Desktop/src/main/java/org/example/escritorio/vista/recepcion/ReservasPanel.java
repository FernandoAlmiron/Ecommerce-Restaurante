package org.example.escritorio.vista.recepcion;

import org.example.Modelo.enums.EstadoReserva;
import org.example.dto.ReservaResponseDTO;
import org.example.escritorio.controlador.recepcion.ReservasControlador;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.comun.Icono;
import org.example.escritorio.vista.comun.Selector;
import org.example.escritorio.vista.comun.Tablas;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Pantalla de Recepción: lista de reservas con filtros y las acciones de cada reserva.
 * Solo dibuja; lo que pasa al hacer clic lo decide ReservasControlador.
 */
public class ReservasPanel extends JPanel {

    private static final int ALTO_FILA = 58;

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
        JPanel textos = Estilo.tituloPantalla("Recepción", "Reservas");

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
                + " borderColor: " + Estilo.hex(Estilo.BORDE_CAMPO) + "; focusedBorderColor: " + Estilo.hex(Estilo.CAFE));

        chkSoloHoy.setFont(Estilo.fuente(Font.PLAIN, 16f));
        chkSoloHoy.setForeground(Estilo.TEXTO);
        chkSoloHoy.setOpaque(false);
        chkSoloHoy.setIconTextGap(10);
        chkSoloHoy.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Estilo.casilla(chkSoloHoy);

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

    /** Pestañas Todas / Pendientes / Confirmadas / Canceladas. */
    private JComponent armarSelectorEstado() {
        EstadoReserva[] estados = {null, EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA, EstadoReserva.CANCELADA};
        Selector selector = new Selector("Todas", "Pendientes", "Confirmadas", "Canceladas");
        selector.alElegir(i -> {
            filtroEstado = estados[i];
            aplicarFiltros();
        });
        return selector;
    }

    private JComponent armarTabla() {
        tabla.setRowSorter(orden);
        orden.setSortKeys(List.of(
                new RowSorter.SortKey(ReservaTableModel.COL_FECHA, SortOrder.ASCENDING),
                new RowSorter.SortKey(ReservaTableModel.COL_HORA, SortOrder.ASCENDING)));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Tablas.estilizar(tabla, ALTO_FILA);

        Font normal = Estilo.fuente(Font.PLAIN, 16f);
        Font negrita = Estilo.fuente(Font.BOLD, 16f);
        Tablas.renderer(tabla, 0, new Tablas.RendererTexto(normal, Estilo.TEXTO_SUAVE, v -> "#" + v));
        Tablas.renderer(tabla, 1, new Tablas.RendererTexto(normal, Estilo.TEXTO, v -> Formatos.fecha((LocalDate) v)));
        Tablas.renderer(tabla, 2, new Tablas.RendererTexto(negrita, Estilo.TEXTO, v -> Formatos.hora((LocalTime) v)));
        Tablas.renderer(tabla, 3, new Tablas.RendererTexto(normal, Estilo.TEXTO, String::valueOf));
        Tablas.renderer(tabla, 4, new Tablas.RendererTexto(normal, Estilo.TEXTO, String::valueOf));
        Tablas.renderer(tabla, 5, new Tablas.RendererTexto(normal, Estilo.TEXTO, String::valueOf));
        Tablas.renderer(tabla, 6, new Tablas.RendererInsignia(v -> switch ((String) v) {
            case ReservaTableModel.CONFIRMADA -> Tablas.Insignia.verde(ReservaTableModel.CONFIRMADA);
            case ReservaTableModel.PENDIENTE -> Tablas.Insignia.amarilla(ReservaTableModel.PENDIENTE);
            case ReservaTableModel.EN_MESA -> Tablas.Insignia.violeta(ReservaTableModel.EN_MESA);
            case ReservaTableModel.ASISTIO -> Tablas.Insignia.azul(ReservaTableModel.ASISTIO);
            case ReservaTableModel.NO_ASISTIO -> Tablas.Insignia.roja(ReservaTableModel.NO_ASISTIO);
            default -> Tablas.Insignia.gris(ReservaTableModel.CANCELADA);
        }));
        Tablas.renderer(tabla, 7, new RendererSenia());
        Tablas.renderer(tabla, 8, new Tablas.RendererTexto(normal, Estilo.TEXTO, String::valueOf));
        Tablas.anchos(tabla, 110, 120, 100, 220, 130, 140, 170, 140, 110);
        columnaFecha = tabla.getColumnModel().getColumn(ReservaTableModel.COL_FECHA);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(Tablas.scroll(tabla), BorderLayout.CENTER);

        // la tarjeta mide lo que ocupan las filas (como mucho, el alto disponible)
        JPanel contenedor = Estilo.transparente(new Tablas.Apilado(0, () -> Tablas.altoContenido(tabla)));
        contenedor.add(tarjeta);
        return contenedor;
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

    /** Las reservas con mesa abierta: se muestran "En mesa" y no se pueden cancelar. */
    public void setReservasConMesa(java.util.Set<Integer> conMesa) {
        modelo.setReservasConMesa(conMesa);
    }

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

    // ---------- piezas visuales ----------

    /** Seña: el monto, o "Sin seña" en gris. En las canceladas el monto va en gris. */
    private static class RendererSenia extends Tablas.RendererTexto {
        RendererSenia() {
            super(Estilo.fuente(Font.PLAIN, 16f), Estilo.TEXTO, v -> Tablas.pesosCortos((BigDecimal) v));
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
            ReservaResponseDTO r = ((ReservaTableModel) tabla.getModel())
                    .getFila(tabla.convertRowIndexToModel(fila));
            if (valor == null) {
                setText("Sin seña");
            } else if (r.estado() == EstadoReserva.AUSENTE) {
                setText(getText() + " · cobrada");
                setForeground(Estilo.ROJO_TEXTO);
            }
            if (valor == null || r.estado() == EstadoReserva.CANCELADA) {
                setForeground(Estilo.TEXTO_SUAVE);
            }
            return this;
        }
    }
}
