package org.example.escritorio.vista.cocina;

import org.example.dto.ComandaDTO;
import org.example.escritorio.controlador.cocina.ComandasControlador;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Formatos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.Timer;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Comandas para la cocina (RF-23, RF-24): los tickets abiertos que tienen pedidos, de salón y de delivery.
 * A la izquierda la lista; a la derecha lo que hay que preparar del ticket elegido, sin precios.
 * Se actualiza sola cada 10 segundos mientras la pantalla está a la vista.
 */
public class ComandasPanel extends JPanel {

    private static final int REFRESCO_MS = 10_000;

    private final ComandaTableModel modeloComandas = new ComandaTableModel();
    private final JTable tablaComandas = new JTable(modeloComandas);
    private final ComandaItemTableModel modeloItems = new ComandaItemTableModel();
    private final JTable tablaItems = new JTable(modeloItems);

    private final JLabel lblDetalle = new JLabel("Elegí una comanda para ver qué hay que preparar.");
    private final JLabel lblEstado = new JLabel(" ");
    private final JButton btnActualizar = new JButton("Actualizar");

    private final ComandasControlador controlador;
    private final Timer refresco;

    public ComandasPanel() {
        super(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, armarLista(), armarDetalle());
        division.setResizeWeight(0.5);
        division.setBorder(null);

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(lblEstado, BorderLayout.WEST);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(btnActualizar);
        arriba.add(derecha, BorderLayout.EAST);

        add(arriba, BorderLayout.NORTH);
        add(division, BorderLayout.CENTER);

        controlador = new ComandasControlador(this);

        tablaComandas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                controlador.alCambiarSeleccion();
            }
        });
        btnActualizar.addActionListener(e -> controlador.cargar(true));

        // solo refresca si la solapa está a la vista; el timer se apaga al cerrar la ventana
        refresco = new Timer(REFRESCO_MS, e -> {
            if (isShowing()) {
                controlador.cargar(false);
            }
        });

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

    private JScrollPane armarLista() {
        TableRowSorter<ComandaTableModel> orden = new TableRowSorter<>(modeloComandas);
        orden.setSortKeys(List.of(new RowSorter.SortKey(ComandaTableModel.COL_TICKET, SortOrder.ASCENDING)));
        tablaComandas.setRowSorter(orden);
        tablaComandas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaComandas.setFillsViewportHeight(true);
        tablaComandas.setRowHeight(26);
        Formatos.aplicarA(tablaComandas);
        int[] anchos = {60, 80, 150, 90, 60};
        for (int i = 0; i < anchos.length; i++) {
            tablaComandas.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        return new JScrollPane(tablaComandas);
    }

    private JPanel armarDetalle() {
        lblDetalle.setFont(lblDetalle.getFont().deriveFont(Font.BOLD, 14f));
        tablaItems.setFillsViewportHeight(true);
        tablaItems.setRowHeight(28);
        tablaItems.setRowSelectionAllowed(false);
        tablaItems.setFont(tablaItems.getFont().deriveFont(14f));
        tablaItems.getColumnModel().getColumn(0).setPreferredWidth(220);
        tablaItems.getColumnModel().getColumn(1).setPreferredWidth(70);
        tablaItems.getColumnModel().getColumn(2).setPreferredWidth(220);

        JPanel detalle = new JPanel(new BorderLayout(0, 8));
        detalle.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        detalle.add(lblDetalle, BorderLayout.NORTH);
        detalle.add(new JScrollPane(tablaItems), BorderLayout.CENTER);
        return detalle;
    }

    // ---------- lo que usa el controlador ----------

    /** Muestra las comandas y deja seleccionado el mismo ticket que estaba elegido, si sigue abierto. */
    public void mostrarComandas(List<ComandaDTO> comandas) {
        Integer elegido = comandaSeleccionada().map(ComandaDTO::nroTicket).orElse(null);
        modeloComandas.setFilas(comandas);
        if (elegido != null) {
            for (int fila = 0; fila < tablaComandas.getRowCount(); fila++) {
                if (modeloComandas.getFila(tablaComandas.convertRowIndexToModel(fila)).nroTicket() == elegido) {
                    tablaComandas.setRowSelectionInterval(fila, fila);
                    break;
                }
            }
        }
        lblEstado.setForeground(getForeground());
        lblEstado.setText(comandas.size() + (comandas.size() == 1 ? " comanda" : " comandas")
                + " · actualizado a las " + Formatos.hora(LocalTime.now()));
    }

    public Optional<ComandaDTO> comandaSeleccionada() {
        int filaVista = tablaComandas.getSelectedRow();
        if (filaVista < 0) {
            return Optional.empty();
        }
        return Optional.of(modeloComandas.getFila(tablaComandas.convertRowIndexToModel(filaVista)));
    }

    public void mostrarDetalle(ComandaDTO comanda) {
        String titulo = "Ticket N° " + comanda.nroTicket();
        if (comanda.nroReserva() != null) {
            titulo += " · " + comanda.zona() + " · " + comanda.comensales() + " comensales";
        }
        lblDetalle.setText(titulo);
        modeloItems.setFilas(comanda.pedidos());
    }

    public void limpiarDetalle() {
        lblDetalle.setText("Elegí una comanda para ver qué hay que preparar.");
        modeloItems.setFilas(List.of());
    }

    /** Si falla el refresco automático no se abre un diálogo cada 10 segundos: se avisa arriba. */
    public void mostrarAviso(String mensaje) {
        lblEstado.setForeground(new Color(0xC62828));
        lblEstado.setText(mensaje);
    }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public JButton getBotonActualizar() { return btnActualizar; }
}