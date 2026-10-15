package org.example.escritorio.vista.recepcion;

import org.example.Modelo.enums.EstadoReserva;
import org.example.dto.ReservaResponseDTO;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.comun.ModeloTablaBase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Columnas de la tabla de reservas. */
public class ReservaTableModel extends ModeloTablaBase<ReservaResponseDTO> {

    public static final int COL_FECHA = 1;
    public static final int COL_HORA = 2;
    public static final int COL_ESTADO = 6;

    public ReservaTableModel() {
        super("N°", "Fecha", "Hora", "Cliente", "Zona", "Comensales", "Estado", "Seña", "Asistió");
    }

    @Override
    protected Object valor(ReservaResponseDTO r, int columna) {
        return switch (columna) {
            case 0 -> r.nroReserva();
            case 1 -> r.fecha();
            case 2 -> r.hora();
            case 3 -> r.cliente();
            case 4 -> r.zona();
            case 5 -> r.comensales();
            case 6 -> r.estado();
            case 7 -> r.senia();
            case 8 -> asistencia(r);
            default -> null;
        };
    }

    @Override
    protected Class<?> tipo(int columna) {
        return switch (columna) {
            case 0, 5 -> Integer.class;
            case 1 -> LocalDate.class;
            case 2 -> LocalTime.class;
            case 6 -> EstadoReserva.class;
            case 7 -> BigDecimal.class;
            default -> String.class;
        };
    }

    /** La asistencia solo tiene sentido en reservas confirmadas con seña (los walk-in no tienen). */
    private String asistencia(ReservaResponseDTO r) {
        if (r.estado() != EstadoReserva.CONFIRMADA || r.senia() == null) {
            return "—";
        }
        return Formatos.siNo(r.asistio());
    }
}