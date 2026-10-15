package org.example.escritorio.vista.cocina;

import org.example.Modelo.inventario.Stock;
import org.example.Modelo.menu.Menu;
import org.example.Modelo.menu.MenuIngrediente;
import org.example.escritorio.controlador.cocina.RecetaControlador;
import org.example.escritorio.vista.comun.Dialogos;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

/**
 * Receta de un plato (RF-28): qué insumos lleva y cuánto por porción.
 * Es lo que PedidoService usa para descontar stock cuando se carga un pedido (RF-29).
 */
public class RecetaDialog extends JDialog {

    private final Menu plato;

    private final RecetaTableModel modelo = new RecetaTableModel();
    private final JTable tabla = new JTable(modelo);

    private final JComboBox<Stock> cmbInsumo = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(0.1, 0.001, 100_000d, 0.1));
    private final JLabel lblUnidad = new JLabel(" ");
    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnCambiarCantidad = new JButton("Cambiar cantidad");
    private final JButton btnQuitar = new JButton("Quitar");
    private final JButton btnCerrar = new JButton("Cerrar");

    private final RecetaControlador controlador;

    public RecetaDialog(Window padre, Menu plato) {
        super(padre, "Receta — " + plato.getNombre(), ModalityType.APPLICATION_MODAL);
        this.plato = plato;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JLabel titulo = new JLabel("Receta de " + plato.getNombre());
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        JLabel ayuda = new JLabel("Cantidades por porción. Al cargar un pedido se descuenta cantidad × unidades pedidas.");

        JPanel encabezado = new JPanel(new BorderLayout(0, 4));
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(ayuda, BorderLayout.CENTER);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(26);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(600, 260));

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.add(scroll, BorderLayout.CENTER);
        centro.add(armarAccionesFila(), BorderLayout.SOUTH);

        JPanel raiz = new JPanel(new BorderLayout(0, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        raiz.add(encabezado, BorderLayout.NORTH);
        raiz.add(centro, BorderLayout.CENTER);
        raiz.add(armarAgregar(), BorderLayout.SOUTH);
        setContentPane(raiz);

        spnCantidad.setEditor(new JSpinner.NumberEditor(spnCantidad, "0.###"));
        cmbInsumo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof Stock s) {
                    setText(s.getNombre() + " (" + s.getUnidadMedida() + ")");
                }
                return this;
            }
        });

        controlador = new RecetaControlador(this);

        cmbInsumo.addActionListener(e -> {
            Stock s = getInsumoElegido();
            lblUnidad.setText(s == null ? " " : s.getUnidadMedida());
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                habilitarAccionesFila(ingredienteSeleccionado().isPresent());
            }
        });
        btnAgregar.addActionListener(e -> controlador.agregar());
        btnCambiarCantidad.addActionListener(e -> controlador.cambiarCantidad());
        btnQuitar.addActionListener(e -> controlador.quitar());
        btnCerrar.addActionListener(e -> dispose());

        habilitarAccionesFila(false);
        controlador.cargar();

        pack();
        setLocationRelativeTo(padre);
    }

    private JPanel armarAccionesFila() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnQuitar);
        botones.add(btnCambiarCantidad);
        return botones;
    }

    private JPanel armarAgregar() {
        JPanel agregar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        agregar.setBorder(BorderFactory.createTitledBorder("Agregar insumo"));
        agregar.add(cmbInsumo);
        agregar.add(new JLabel("Cantidad"));
        agregar.add(spnCantidad);
        agregar.add(lblUnidad);
        agregar.add(btnAgregar);

        JPanel cerrar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        cerrar.add(btnCerrar);

        JPanel pie = new JPanel(new BorderLayout(0, 12));
        pie.add(agregar, BorderLayout.CENTER);
        pie.add(cerrar, BorderLayout.SOUTH);
        return pie;
    }

    // ---------- lo que usa el controlador ----------

    public Menu getPlato() { return plato; }
    public Stock getInsumoElegido() { return (Stock) cmbInsumo.getSelectedItem(); }

    /** La cantidad escrita; null si no es un número válido. */
    public Double getCantidad() {
        try {
            spnCantidad.commitEdit();
        } catch (ParseException e) {
            return null;
        }
        return ((Number) spnCantidad.getValue()).doubleValue();
    }

    public JButton getBotonAgregar() { return btnAgregar; }
    public JButton getBotonCambiarCantidad() { return btnCambiarCantidad; }
    public JButton getBotonQuitar() { return btnQuitar; }
    public JTable getTabla() { return tabla; }

    /** Muestra la receta y, en el combo, solo los insumos que todavía no están en ella. */
    public void mostrarReceta(List<MenuIngrediente> ingredientes, List<Stock> insumosParaAgregar) {
        modelo.setFilas(ingredientes);
        cmbInsumo.removeAllItems();
        insumosParaAgregar.forEach(cmbInsumo::addItem);
        btnAgregar.setEnabled(!insumosParaAgregar.isEmpty());
        spnCantidad.setValue(0.1);
        habilitarAccionesFila(ingredienteSeleccionado().isPresent());
    }

    public Optional<MenuIngrediente> ingredienteSeleccionado() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? Optional.empty() : Optional.of(modelo.getFila(tabla.convertRowIndexToModel(fila)));
    }

    public void habilitarAccionesFila(boolean hayIngrediente) {
        btnCambiarCantidad.setEnabled(hayIngrediente);
        btnQuitar.setEnabled(hayIngrediente);
    }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public boolean confirmar(String pregunta) {
        return Dialogos.confirmar(this, pregunta);
    }
}