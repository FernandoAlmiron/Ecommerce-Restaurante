package org.example.escritorio.vista.salon;

import org.example.Modelo.menu.Pedido;
import org.example.escritorio.vista.comun.ModeloTablaBase;

import java.math.BigDecimal;

/** Columnas de la tabla de pedidos de un ticket. El precio lo puso el backend al cargar el pedido. */
public class PedidoTableModel extends ModeloTablaBase<Pedido> {

    public PedidoTableModel() {
        super("Plato", "Cantidad", "Precio unitario", "Subtotal", "Observaciones");
    }

    @Override
    protected Object valor(Pedido p, int columna) {
        return switch (columna) {
            case 0 -> p.getMenu().getNombre();
            case 1 -> p.getCantidad();
            case 2 -> p.getPrecioUnitario();
            case 3 -> p.calcularSubtotal();
            case 4 -> p.getObservaciones() == null ? "" : p.getObservaciones();
            default -> null;
        };
    }

    @Override
    protected Class<?> tipo(int columna) {
        return switch (columna) {
            case 1 -> Integer.class;
            case 2, 3 -> BigDecimal.class;
            default -> String.class;
        };
    }
}