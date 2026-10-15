package org.example.escritorio.vista.reparto;

import org.example.Modelo.enums.EstadoEnvio;
import org.example.dto.RemitoDTO;
import org.example.escritorio.controlador.reparto.RepartoControlador;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Formatos;

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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Optional;

/**
 * Reparto (RF-36 a RF-39): los envíos del repartidor logueado (el ADMIN ve todos),
 * con los cambios de estado y el remito. Asignar repartidor es solo para ADMIN (RF-36).
 */
public class RepartoPanel extends JPanel {

    private final EnvioTableModel modelo = new EnvioTableModel();
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<EnvioTableModel> orden = new TableRowSorter<>(modelo);

    private final JCheckBox chkSoloActivos = new JCheckBox("Solo pendientes y en camino", true);
    private final JButton btnActualizar = new JButton("Actualizar");
    private final JLabel lblResumen = new JLabel(" ");

    private final JButton btnAsignar = new JButton("Asignar repartidor");
    private final JButton btnRemito = new JButton("Ver remito");
    private final JButton btnEnCamino = new JButton("Salió (en camino)");
    private final JButton btnEntregado = new JButton("Entregado");
    private final JButton btnCancelar = new JButton("Cancelar envío");

    private final RepartoControlador controlador;

    public RepartoPanel() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(armarEncabezado(), BorderLayout.NORTH);
        add(armarTabla(), BorderLayout.CENTER);
        add(armarAcciones(), BorderLayout.SOUTH);

        controlador = new RepartoControlador(this);

        chkSoloActivos.addActionListener(e -> aplicarFiltro());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                controlador.alCambiarSeleccion();
            }
        });
        btnActualizar.addActionListener(e -> controlador.cargar(null));
        btnAsignar.addActionListener(e -> controlador.asignarRepartidor());
        btnRemito.addActionListener(e -> controlador.verRemito());
        btnEnCamino.addActionListener(e -> controlador.cambiarEstado(EstadoEnvio.EN_CAMINO));
        btnEntregado.addActionListener(e -> controlador.cambiarEstado(EstadoEnvio.ENTREGADO));
        btnCancelar.addActionListener(e -> controlador.cambiarEstado(EstadoEnvio.CANCELADO));

        habilitarAcciones(false, false, false, false, false);
        controlador.cargar(null);
    }

    // ---------- armado ----------

    private JPanel armarEncabezado() {
        JLabel titulo = new JLabel(Sesion.esAdmin() ? "Reparto · todos los envíos" : "Reparto · mis envíos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.add(chkSoloActivos);
        filtros.add(btnActualizar);

        JPanel encabezado = new JPanel(new BorderLayout(0, 12));
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(filtros, BorderLayout.CENTER);
        return encabezado;
    }

    private JScrollPane armarTabla() {
        orden.setSortKeys(List.of(new RowSorter.SortKey(EnvioTableModel.COL_ENVIO, SortOrder.ASCENDING)));
        tabla.setRowSorter(orden);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(26);
        Formatos.aplicarA(tabla);
        tabla.getColumnModel().getColumn(EnvioTableModel.COL_ESTADO).setCellRenderer(new RendererEstado());
        int[] anchos = {50, 50, 140, 210, 50, 100, 120, 90, 130};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        return new JScrollPane(tabla);
    }

    private JPanel armarAcciones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        if (Sesion.esAdmin()) {
            botones.add(btnAsignar);
        }
        botones.add(btnRemito);
        botones.add(btnCancelar);
        botones.add(btnEnCamino);
        botones.add(btnEntregado);

        JPanel acciones = new JPanel(new BorderLayout());
        acciones.add(lblResumen, BorderLayout.WEST);
        acciones.add(botones, BorderLayout.EAST);
        return acciones;
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
                return;
            }
        }
    }

    public void habilitarAcciones(boolean asignar, boolean remito, boolean enCamino,
                                  boolean entregado, boolean cancelar) {
        btnAsignar.setEnabled(asignar);
        btnRemito.setEnabled(remito);
        btnEnCamino.setEnabled(enCamino);
        btnEntregado.setEnabled(entregado);
        btnCancelar.setEnabled(cancelar);
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
        orden.setRowFilter(chkSoloActivos.isSelected() ? new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends EnvioTableModel, ? extends Integer> fila) {
                EstadoEnvio e = fila.getModel().getFila(fila.getIdentifier()).estado();
                return e == EstadoEnvio.PENDIENTE || e == EstadoEnvio.EN_CAMINO;
            }
        } : null);

        int pendientes = 0;
        int enCamino = 0;
        for (int i = 0; i < modelo.getRowCount(); i++) {
            EstadoEnvio e = modelo.getFila(i).estado();
            if (e == EstadoEnvio.PENDIENTE) {
                pendientes++;
            } else if (e == EstadoEnvio.EN_CAMINO) {
                enCamino++;
            }
        }
        lblResumen.setText(pendientes + (pendientes == 1 ? " pendiente" : " pendientes")
                + " · " + enCamino + " en camino");
    }

    /** Pendiente en naranja, en camino en azul, entregado en verde, cancelado en gris. */
    private static class RendererEstado extends DefaultTableCellRenderer {
        @Override
        protected void setValue(Object valor) {
            if (!(valor instanceof EstadoEnvio estado)) {
                setText("");
                return;
            }
            setText(EnvioTableModel.texto(estado));
            setForeground(switch (estado) {
                case PENDIENTE -> new Color(0xB26A00);
                case EN_CAMINO -> new Color(0x1565C0);
                case ENTREGADO -> new Color(0x2E7D32);
                case CANCELADO -> Color.GRAY;
            });
        }
    }
}