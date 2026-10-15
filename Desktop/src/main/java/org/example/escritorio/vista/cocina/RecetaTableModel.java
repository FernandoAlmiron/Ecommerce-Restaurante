package org.example.escritorio.vista.cocina;

import org.example.Modelo.menu.MenuIngrediente;
import org.example.escritorio.vista.comun.ModeloTablaBase;

/** Columnas de la receta de un plato: qué insumo y cuánto lleva una porción. */
public class RecetaTableModel extends ModeloTablaBase<MenuIngrediente> {

    public RecetaTableModel() {
        super("Insumo", "Cantidad por porción", "Unidad", "Stock actual");
    }

    @Override
    protected Object valor(MenuIngrediente mi, int columna) {
        return switch (columna) {
            case 0 -> mi.getStock().getNombre();
            case 1 -> mi.getCantidadNecesaria();
            case 2 -> mi.getStock().getUnidadMedida();
            case 3 -> mi.getStock().getCantidadActual();
            default -> null;
        };
    }

    @Override
    protected Class<?> tipo(int columna) {
        return columna == 1 || columna == 3 ? Double.class : String.class;
    }
}