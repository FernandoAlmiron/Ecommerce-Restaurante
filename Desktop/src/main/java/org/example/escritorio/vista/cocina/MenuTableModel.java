package org.example.escritorio.vista.cocina;

import org.example.Modelo.menu.Menu;
import org.example.escritorio.vista.comun.ModeloTablaBase;

import java.math.BigDecimal;

/** Columnas de la carta. Disponible se ve como casilla (Boolean). */
public class MenuTableModel extends ModeloTablaBase<Menu> {

    public static final int COL_NOMBRE = 0;

    public MenuTableModel() {
        super("Plato", "Descripción", "Precio", "Disponible");
    }

    @Override
    protected Object valor(Menu m, int columna) {
        return switch (columna) {
            case 0 -> m.getNombre();
            case 1 -> m.getDescripcion() == null ? "" : m.getDescripcion();
            case 2 -> m.getPrecio();
            case 3 -> m.isDisponible();
            default -> null;
        };
    }

    @Override
    protected Class<?> tipo(int columna) {
        return switch (columna) {
            case 2 -> BigDecimal.class;
            case 3 -> Boolean.class;
            default -> String.class;
        };
    }
}