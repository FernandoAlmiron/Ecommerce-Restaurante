package org.example.escritorio.vista.caja;

import org.example.Modelo.enums.OrigenTicket;
import org.example.escritorio.vista.comun.ModeloTablaBase;

import java.math.BigDecimal;

/** Columnas de la lista de cobros. */
public class CobroTableModel extends ModeloTablaBase<CobroFila> {

    public static final int COL_TICKET = 0;
    public static final int COL_ESTADO = 6;

    public CobroTableModel() {
        super("Ticket", "Origen", "Cliente", "Pedidos", "Consumo", "A pagar", "Estado");
    }

    @Override
    protected Object valor(CobroFila c, int columna) {
        return switch (columna) {
            case 0 -> c.nroTicket();
            case 1 -> c.origen() == OrigenTicket.DELIVERY ? "Delivery" : "Salón";
            case 2 -> c.cliente();
            case 3 -> c.cantidadPedidos();
            case 4 -> c.consumo();
            case 5 -> c.aPagar();
            case 6 -> c.estado();
            default -> null;
        };
    }

    @Override
    protected Class<?> tipo(int columna) {
        return switch (columna) {
            case 0, 3 -> Integer.class;
            case 4, 5 -> BigDecimal.class;
            default -> String.class;
        };
    }
}