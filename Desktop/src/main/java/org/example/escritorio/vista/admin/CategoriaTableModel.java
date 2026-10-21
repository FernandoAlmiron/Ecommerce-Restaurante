package org.example.escritorio.vista.admin;

import org.example.Modelo.inventario.Categoria;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.comun.ModeloTablaBase;

/** Columnas de la lista de categorías de insumos y proveedores (RF-56). */
public class CategoriaTableModel extends ModeloTablaBase<Categoria> {

    public CategoriaTableModel() {
        super("Categoría", "Descripción", "Activa");
    }

    @Override
    protected Object valor(Categoria c, int columna) {
        return switch (columna) {
            case 0 -> c.getNombre();
            case 1 -> c.getDescripcion() == null ? "" : c.getDescripcion();
            case 2 -> Formatos.siNo(c.isActivo());
            default -> null;
        };
    }
}