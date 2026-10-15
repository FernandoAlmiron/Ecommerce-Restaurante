package org.example.escritorio.vista.cocina;

import org.example.dto.ComandaDTO;
import org.example.Modelo.enums.OrigenTicket;
import org.example.escritorio.vista.comun.ModeloTablaBase;

/** Columnas de la lista de comandas: una fila por ticket abierto con pedidos. */
public class ComandaTableModel extends ModeloTablaBase<ComandaDTO> {

    public static final int COL_TICKET = 0;

    public ComandaTableModel() {
        super("Ticket", "Origen", "Zona", "Comensales", "Platos");
    }

    @Override
    protected Object valor(ComandaDTO c, int columna) {
        return switch (columna) {
            case 0 -> c.nroTicket();
            case 1 -> c.origen() == OrigenTicket.DELIVERY ? "Delivery" : "Salón";
            case 2 -> c.zona() != null ? c.zona() : (c.origen() == OrigenTicket.DELIVERY ? "—" : "Sin reserva");
            case 3 -> c.comensales();
            case 4 -> c.pedidos().stream().mapToInt(ComandaDTO.Item::cantidad).sum();
            default -> null;
        };
    }

    @Override
    protected Class<?> tipo(int columna) {
        return switch (columna) {
            case 0, 3, 4 -> Integer.class;
            default -> String.class;
        };
    }
}