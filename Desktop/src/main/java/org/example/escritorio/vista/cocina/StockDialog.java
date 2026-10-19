package org.example.escritorio.vista.cocina;

import org.example.Modelo.inventario.Categoria;
import org.example.Modelo.inventario.Stock;
import org.example.escritorio.controlador.cocina.StockEdicionControlador;
import org.example.escritorio.vista.comun.DialogoFormulario;
import org.example.escritorio.vista.comun.Formulario;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.Component;
import java.awt.Window;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

/**
 * Alta o modificación de un insumo (RF-30). Con insumo null es un alta.
 * La cantidad actual solo se carga en el alta: después se cambia con Reponer o Ajustar.
 */
public class StockDialog extends DialogoFormulario {

    private final Stock insumo;

    private final JTextField txtNombre = new JTextField(24);
    private final JTextField txtUnidad = new JTextField(8);
    private final JComboBox<Categoria> cmbCategoria = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(0d, 0d, 1_000_000d, 1d));
    private final JSpinner spnMinimo = new JSpinner(new SpinnerNumberModel(0d, 0d, 1_000_000d, 1d));
    private final JCheckBox chkAlerta = new JCheckBox("Avisar cuando llegue al mínimo", true);

    private final StockEdicionControlador controlador;
    private Stock guardado;

    public StockDialog(Window padre, Stock insumo) {
        super(padre, "Cocina · Stock",
                insumo == null ? "Nuevo insumo" : "Modificar insumo",
                insumo == null ? "Completá los datos del insumo para controlar su stock." : insumo.getNombre(),
                "Guardar");
        this.insumo = insumo;

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

        if (insumo != null) {
            txtNombre.setText(insumo.getNombre());
            txtUnidad.setText(insumo.getUnidadMedida());
            spnCantidad.setValue(insumo.getCantidadActual());
            spnCantidad.setEnabled(false);
            spnMinimo.setValue(insumo.getStockMinimo());
            chkAlerta.setSelected(insumo.isStockMinimoActivo());
        }

        controlador = new StockEdicionControlador(this);
        btnPrincipal.addActionListener(e -> controlador.guardar());
        controlador.cargarCategorias();

        armar();
    }

    @Override
    protected JComponent armarCuerpo() {
        Formulario.estilizar(txtNombre, "Ej.: Harina 000");
        Formulario.estilizar(txtUnidad, "kg, lt, unidad");
        Formulario.estilizar(cmbCategoria);
        Formulario.estilizar(spnCantidad);
        Formulario.estilizar(spnMinimo);
        return Formulario.cuerpo(
                Formulario.campo("Nombre", txtNombre, null),
                Formulario.enFila(
                        Formulario.campo("Categoría", cmbCategoria, null),
                        Formulario.campo("Unidad", txtUnidad, null)),
                Formulario.enFila(
                        Formulario.campo(insumo == null ? "Cantidad inicial" : "Cantidad actual", spnCantidad,
                                insumo == null ? null : "Se cambia con Reponer o Ajustar."),
                        Formulario.campo("Stock mínimo", spnMinimo, null)),
                Formulario.opcion(chkAlerta, "El insumo se marca en rojo cuando llega al mínimo."));
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

    public JButton getBotonGuardar() { return btnPrincipal; }
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