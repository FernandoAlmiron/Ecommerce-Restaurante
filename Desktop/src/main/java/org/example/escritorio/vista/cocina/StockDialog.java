package org.example.escritorio.vista.cocina;

import org.example.Modelo.inventario.Categoria;
import org.example.Modelo.inventario.Stock;
import org.example.escritorio.controlador.cocina.StockEdicionControlador;
import org.example.escritorio.vista.comun.Dialogos;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

/**
 * Alta o modificación de un insumo (RF-30). Con insumo null es un alta.
 * La cantidad actual solo se carga en el alta: después se cambia con Reponer o Ajustar.
 */
public class StockDialog extends JDialog {

    private final Stock insumo;

    private final JTextField txtNombre = new JTextField(24);
    private final JTextField txtUnidad = new JTextField(8);
    private final JComboBox<Categoria> cmbCategoria = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(0d, 0d, 1_000_000d, 1d));
    private final JSpinner spnMinimo = new JSpinner(new SpinnerNumberModel(0d, 0d, 1_000_000d, 1d));
    private final JCheckBox chkAlerta = new JCheckBox("Avisar cuando llegue al mínimo", true);
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnCancelar = new JButton("Cancelar");

    private final StockEdicionControlador controlador;
    private Stock guardado;

    public StockDialog(Window padre, Stock insumo) {
        super(padre, insumo == null ? "Nuevo insumo" : "Modificar insumo", ModalityType.APPLICATION_MODAL);
        this.insumo = insumo;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        spnCantidad.setEditor(new JSpinner.NumberEditor(spnCantidad, "0.###"));
        spnMinimo.setEditor(new JSpinner.NumberEditor(spnMinimo, "0.###"));
        cmbCategoria.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof Categoria c) {
                    setText(c.getNombre());
                }
                return this;
            }
        });

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        raiz.add(armarFormulario(), BorderLayout.CENTER);
        raiz.add(armarBotones(), BorderLayout.SOUTH);
        setContentPane(raiz);

        if (insumo != null) {
            txtNombre.setText(insumo.getNombre());
            txtUnidad.setText(insumo.getUnidadMedida());
            spnCantidad.setValue(insumo.getCantidadActual());
            spnCantidad.setEnabled(false);
            spnMinimo.setValue(insumo.getStockMinimo());
            chkAlerta.setSelected(insumo.isStockMinimoActivo());
        }

        controlador = new StockEdicionControlador(this);
        btnGuardar.addActionListener(e -> controlador.guardar());
        btnCancelar.addActionListener(e -> dispose());
        controlador.cargarCategorias();

        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    private JPanel armarFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 12);

        JLabel titulo = new JLabel(insumo == null ? "Nuevo insumo" : insumo.getNombre());
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(titulo, c);
        c.gridwidth = 1;

        fila(panel, c, 1, "Nombre", txtNombre);
        fila(panel, c, 2, "Unidad (kg, lt, unidad)", txtUnidad);
        fila(panel, c, 3, "Categoría", cmbCategoria);
        fila(panel, c, 4, insumo == null ? "Cantidad inicial" : "Cantidad actual", spnCantidad);
        fila(panel, c, 5, "Stock mínimo", spnMinimo);
        c.gridx = 1; c.gridy = 6;
        panel.add(chkAlerta, c);
        return panel;
    }

    private void fila(JPanel panel, GridBagConstraints c, int y, String etiqueta, JComponent campo) {
        c.gridx = 0; c.gridy = y; c.weightx = 0;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1; c.weightx = 1;
        panel.add(campo, c);
    }

    private JPanel armarBotones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnCancelar);
        botones.add(btnGuardar);
        getRootPane().setDefaultButton(btnGuardar);
        return botones;
    }

    // ---------- lo que usa el controlador ----------

    /** null si es un alta. */
    public Stock getInsumo() { return insumo; }
    public String getNombre() { return txtNombre.getText().trim(); }
    public String getUnidad() { return txtUnidad.getText().trim(); }
    public Categoria getCategoria() { return (Categoria) cmbCategoria.getSelectedItem(); }
    public boolean isAlertaActiva() { return chkAlerta.isSelected(); }

    /** null si lo escrito no es un número. */
    public Double getCantidad() { return leer(spnCantidad); }
    public Double getMinimo() { return leer(spnMinimo); }

    public JButton getBotonGuardar() { return btnGuardar; }
    public JComboBox<Categoria> getComboCategoria() { return cmbCategoria; }

    /** Carga las categorías y deja elegida la del insumo, si es una modificación. */
    public void mostrarCategorias(List<Categoria> categorias) {
        cmbCategoria.removeAllItems();
        categorias.forEach(cmbCategoria::addItem);
        if (insumo != null && insumo.getCategoria() != null) {
            for (Categoria c : categorias) {
                if (c.getNroCategoria() == insumo.getCategoria().getNroCategoria()) {
                    cmbCategoria.setSelectedItem(c);
                }
            }
        }
    }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public void cerrarCon(Stock insumoGuardado) {
        guardado = insumoGuardado;
        dispose();
    }

    public Optional<Stock> getGuardado() {
        return Optional.ofNullable(guardado);
    }

    private static Double leer(JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (ParseException e) {
            return null;
        }
        return ((Number) spinner.getValue()).doubleValue();
    }
}