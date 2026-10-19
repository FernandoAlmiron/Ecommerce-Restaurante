package org.example.escritorio.vista.cocina;

import org.example.Modelo.inventario.Stock;
import org.example.escritorio.vista.comun.ModeloTablaBase;

/** Columnas del stock de insumos. Alerta es la casilla "avisar bajo mínimo" del insumo. */
public class StockTableModel extends ModeloTablaBase<Stock> {

    public static final int COL_INSUMO = 0;

    public StockTableModel() {
        super("Insumo", "Categoría", "Unidad", "Cantidad", "Mínimo", "Alerta");
    }

    @Override
    protected Object valor(Stock s, int columna) {
        return switch (columna) {
            case 0 -> s.getNombre();
            case 1 -> s.getCategoria() == null ? "" : s.getCategoria().getNombre();
            case 2 -> s.getUnidadMedida();
            case 3 -> s.getCantidadActual();
            case 4 -> s.getStockMinimo();
            case 5 -> s.isStockMinimoActivo();
            default -> null;
        };
    }

    @Override
    protected Class<?> tipo(int columna) {
        return switch (columna) {
            case 3, 4 -> Double.class;
            case 5 -> Boolean.class;
            default -> String.class;
        };
    }
}