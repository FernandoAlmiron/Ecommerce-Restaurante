package org.example.escritorio.vista.recepcion;

import org.example.Modelo.enums.EstadoReserva;
import org.example.dto.ReservaResponseDTO;
import org.example.escritorio.controlador.recepcion.ReservasControlador;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Formatos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Pantalla de Recepción: lista de reservas con filtros y las acciones de cada reserva.
 * Solo dibuja; lo que pasa al hacer clic lo decide ReservasControlador.
 */
public class ReservasPanel extends JPanel {

    private static final String TODAS = "Todas";

    private final ReservaTableModel modelo = new ReservaTableModel();
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<ReservaTableModel> orden = new TableRowSorter<>(modelo);

    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{
            TODAS, EstadoReserva.PENDIENTE.name(), EstadoReserva.CONFIRMADA.name(), EstadoReserva.CANCELADA.name()});
    private final JCheckBox chkSoloHoy = new JCheckBox("Solo hoy", true);
    private final JTextField txtBuscar = new JTextField(14);
    private final JButton btnActualizar = new JButton("Actualizar");
    private final JButton btnWalkIn = new JButton("Nuevo walk-in");

    private final JButton btnConfirmar = new JButton("Confirmar");
    private final JButton btnCancelar = new JButton("Cancelar reserva");
    private final JButton btnAsistio = new JButton("Asistió");
    private final JButton btnNoAsistio = new JButton("No asistió");
    private final JLabel lblResumen = new JLabel(" ");

    private final ReservasControlador controlador;

    public ReservasPanel() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(armarEncabezado(), BorderLayout.NORTH);
        add(armarTabla(), BorderLayout.CENTER);
        add(armarAcciones(), BorderLayout.SOUTH);

        controlador = new ReservasControlador(this);

        // filtros: se aplican sobre lo que ya está cargado, sin volver a pedir datos
        cmbEstado.addActionListener(e -> aplicarFiltros());
        chkSoloHoy.addActionListener(e -> aplicarFiltros());
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override public void removeUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override public void changedUpdate(DocumentEvent e) { aplicarFiltros(); }
        });

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                controlador.alCambiarSeleccion();
            }
        });
        btnActualizar.addActionListener(e -> controlador.cargar());
        btnWalkIn.addActionListener(e -> controlador.nuevoWalkIn());
        btnConfirmar.addActionListener(e -> controlador.confirmar());
        btnCancelar.addActionListener(e -> controlador.cancelar());
        btnAsistio.addActionListener(e -> controlador.marcarAsistencia(true));
        btnNoAsistio.addActionListener(e -> controlador.marcarAsistencia(false));

        habilitarAcciones(false, false, false);
        controlador.cargar();
    }

    // ---------- armado ----------

    private JPanel armarEncabezado() {
        JLabel titulo = new JLabel("Reservas");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.add(new JLabel("Estado:"));
        filtros.add(cmbEstado);
        filtros.add(chkSoloHoy);
        filtros.add(new JLabel("Buscar cliente:"));
        filtros.add(txtBuscar);
        filtros.add(btnActualizar);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(btnWalkIn);

        JPanel fila = new JPanel(new BorderLayout());
        fila.add(filtros, BorderLayout.CENTER);
        fila.add(derecha, BorderLayout.EAST);

        JPanel encabezado = new JPanel(new BorderLayout(0, 12));
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(fila, BorderLayout.CENTER);
        return encabezado;
    }

    private JScrollPane armarTabla() {
        tabla.setRowSorter(orden);
        orden.setSortKeys(List.of(
                new RowSorter.SortKey(ReservaTableModel.COL_FECHA, SortOrder.ASCENDING),
                new RowSorter.SortKey(ReservaTableModel.COL_HORA, SortOrder.ASCENDING)));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(26);
        Formatos.aplicarA(tabla);
        tabla.setDefaultRenderer(EstadoReserva.class, new RendererEstado());

        int[] anchos = {50, 90, 60, 200, 140, 90, 110, 110, 70};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        return new JScrollPane(tabla);
    }

    private JPanel armarAcciones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnConfirmar);
        botones.add(btnCancelar);
        botones.add(btnAsistio);
        botones.add(btnNoAsistio);

        JPanel acciones = new JPanel(new BorderLayout());
        acciones.add(lblResumen, BorderLayout.WEST);
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
        String estado = (String) cmbEstado.getSelectedItem();
        boolean soloHoy = chkSoloHoy.isSelected();
        String texto = txtBuscar.getText().trim().toLowerCase();
        LocalDate hoy = LocalDate.now();

        orden.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends ReservaTableModel, ? extends Integer> fila) {
                ReservaResponseDTO r = fila.getModel().getFila(fila.getIdentifier());
                return (TODAS.equals(estado) || r.estado().name().equals(estado))
                        && (!soloHoy || hoy.equals(r.fecha()))
                        && (texto.isEmpty() || r.cliente().toLowerCase().contains(texto));
            }
        });
        actualizarResumen();
    }

    private void actualizarResumen() {
        int visibles = tabla.getRowCount();
        int total = modelo.getRowCount();
        lblResumen.setText(visibles == total
                ? total + " reservas"
                : "Mostrando " + visibles + " de " + total + " reservas");
    }

    /** Estado con color: pendiente en naranja, confirmada en verde, cancelada en gris. */
    private static class RendererEstado extends DefaultTableCellRenderer {
        @Override
        protected void setValue(Object valor) {
            setText(valor == null ? "" : valor.toString());
            if (valor == EstadoReserva.PENDIENTE) {
                setForeground(new Color(0xB26A00));
            } else if (valor == EstadoReserva.CONFIRMADA) {
                setForeground(new Color(0x2E7D32));
            } else {
                setForeground(Color.GRAY);
            }
        }
    }
}