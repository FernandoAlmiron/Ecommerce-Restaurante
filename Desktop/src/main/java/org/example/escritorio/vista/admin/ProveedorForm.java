package org.example.escritorio.vista.admin;

import org.example.Modelo.inventario.Categoria;
import org.example.Modelo.inventario.Proveedores;
import org.example.escritorio.vista.comun.FormularioSimple;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import java.awt.Component;
import java.util.List;

/**
 * Campos de un proveedor. Con proveedor null es un alta.
 * Las categorías que se ofrecen las decide el controlador.
 */
public class ProveedorForm extends FormularioSimple {

    private final JTextField txtRazonSocial = new JTextField(24);
    private final JTextField txtCuit = new JTextField(14);
    private final JTextField txtTelefono = new JTextField(14);
    private final JTextField txtEmail = new JTextField(24);
    private final JComboBox<Categoria> cmbCategoria = new JComboBox<>();

    public ProveedorForm(Proveedores proveedor, List<Categoria> categorias) {
        super(proveedor == null ? "Nuevo proveedor" : proveedor.getRazonSocial());
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
        categorias.forEach(cmbCategoria::addItem);

        fila("Razón social", txtRazonSocial);
        fila("CUIT", txtCuit);
        fila("Teléfono", txtTelefono);
        fila("Email", txtEmail);
        fila("Categoría", cmbCategoria);
        nota("El CUIT va con sus 11 números; los guiones se pueden escribir o no.");

        if (proveedor != null) {
            txtRazonSocial.setText(proveedor.getRazonSocial());
            txtCuit.setText(ProveedorTableModel.cuit(proveedor.getCuit()));
            txtTelefono.setText(proveedor.getTelefono());
            txtEmail.setText(proveedor.getEmail());
            if (proveedor.getCategoria() != null) {
                for (Categoria c : categorias) {
                    if (c.getNroCategoria() == proveedor.getCategoria().getNroCategoria()) {
                        cmbCategoria.setSelectedItem(c);
                    }
                }
            }
        }
    }

    public String getRazonSocial() { return txtRazonSocial.getText().trim(); }
    /** El CUIT sin guiones, puntos ni espacios. */
    public String getCuit() { return txtCuit.getText().replaceAll("[-.\\s]", ""); }
    public String getTelefono() { return txtTelefono.getText().trim(); }
    public String getEmail() { return txtEmail.getText().trim(); }
    public Categoria getCategoria() { return (Categoria) cmbCategoria.getSelectedItem(); }
}