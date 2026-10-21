package org.example.escritorio.vista.admin;

import org.example.Modelo.inventario.Categoria;
import org.example.escritorio.vista.comun.FormularioSimple;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

/** Campos de una categoría: nombre, descripción y si está activa. Con categoria null es un alta. */
public class CategoriaForm extends FormularioSimple {

    private final JTextField txtNombre = new JTextField(22);
    private final JTextField txtDescripcion = new JTextField(22);
    private final JCheckBox chkActiva = new JCheckBox("Activa (se ofrece en stock y proveedores)", true);

    public CategoriaForm(Categoria categoria) {
        super(categoria == null ? "Nueva categoría" : categoria.getNombre());
        fila("Nombre", txtNombre);
        fila("Descripción", txtDescripcion);
        fila("", chkActiva);
        if (categoria != null) {
            txtNombre.setText(categoria.getNombre());
            txtDescripcion.setText(categoria.getDescripcion());
            chkActiva.setSelected(categoria.isActivo());
        }
    }

    public String getNombre() { return txtNombre.getText().trim(); }
    public String getDescripcion() { return txtDescripcion.getText().trim(); }
    public boolean isActiva() { return chkActiva.isSelected(); }
}