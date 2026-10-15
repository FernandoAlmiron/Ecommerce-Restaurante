package org.example.escritorio.vista.cocina;

import org.example.dto.ComandaDTO;
import org.example.escritorio.vista.comun.ModeloTablaBase;

/** Columnas del detalle de una comanda: lo que hay que preparar, sin precios. */
public class ComandaItemTableModel extends ModeloTablaBase<ComandaDTO.Item> {

    public ComandaItemTableModel() {
        super("Plato", "Cantidad", "Observaciones");
    }

    @Override
    protected Object valor(ComandaDTO.Item item, int columna) {
        return switch (columna) {
            case 0 -> item.plato();
            case 1 -> item.cantidad();
            case 2 -> item.observaciones() == null ? "" : item.observaciones();
            default -> null;
        };
    }

    @Override
    protected Class<?> tipo(int columna) {
        return columna == 1 ? Integer.class : String.class;
    }
}