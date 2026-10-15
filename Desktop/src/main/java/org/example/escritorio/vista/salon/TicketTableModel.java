package org.example.escritorio.vista.salon;

import org.example.escritorio.vista.comun.ModeloTablaBase;

import java.math.BigDecimal;

/** Columnas de la tabla de mesas (tickets de salón). */
public class TicketTableModel extends ModeloTablaBase<TicketFila> {

    public static final int COL_TICKET = 0;

    public TicketTableModel() {
        super("Ticket", "Reserva", "Cliente", "Zona", "Comensales", "Pedidos", "Total", "Estado");
    }

    @Override
    protected Object valor(TicketFila t, int columna) {
        return switch (columna) {
            case 0 -> t.nroTicket();
            case 1 -> t.nroReserva();
            case 2 -> t.cliente();
            case 3 -> t.zona();
            case 4 -> t.comensales();
            case 5 -> t.cantidadPedidos();
            case 6 -> t.total();
            case 7 -> t.estado();
            default -> null;
        };
    }

    @Override
    protected Class<?> tipo(int columna) {
        return switch (columna) {
            case 0, 1, 4, 5 -> Integer.class;
            case 6 -> BigDecimal.class;
            default -> String.class;
        };
    }
}