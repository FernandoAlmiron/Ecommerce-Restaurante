package org.example.escritorio.vista.recepcion;

import org.example.dto.ReservaResponseDTO;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.comun.ModeloTablaBase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/** Columnas de la tabla de reservas. */
public class ReservaTableModel extends ModeloTablaBase<ReservaResponseDTO> {

    // lo que muestra la columna Estado (ver estadoVisible)
    public static final String PENDIENTE = "Pendiente";
    public static final String CONFIRMADA = "Confirmada";
    public static final String EN_MESA = "En mesa";
    public static final String ASISTIO = "Asistió";
    public static final String NO_ASISTIO = "No asistió";
    public static final String CANCELADA = "Cancelada";

    private Set<Integer> conMesa = Set.of();

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
            case 6 -> estadoVisible(r);
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
            case 7 -> BigDecimal.class;
            default -> String.class;
        };
    }

    /** Las reservas con las que ya se abrió una mesa (se muestran "En mesa"). */
    public void setReservasConMesa(Set<Integer> conMesa) {
        this.conMesa = conMesa;
        fireTableDataChanged();
    }

    /** El estado como lo ve Recepción: una confirmada con mesa abierta está "En mesa". */
    public String estadoVisible(ReservaResponseDTO r) {
        return switch (r.estado()) {
            case PENDIENTE -> PENDIENTE;
            case CONFIRMADA -> conMesa.contains(r.nroReserva()) ? EN_MESA : CONFIRMADA;
            case FINALIZADA -> ASISTIO;
            case AUSENTE -> NO_ASISTIO;
            case CANCELADA -> CANCELADA;
        };
    }

    /**
     * Si vino o no, según el estado (la seña arranca con asistio=false y no distingue "no vino" de
     * "todavía no se registró"; además los walk-in no tienen seña).
     */
    private String asistencia(ReservaResponseDTO r) {
        return switch (r.estado()) {
            case FINALIZADA -> Formatos.siNo(true);
            case AUSENTE -> Formatos.siNo(false);
            default -> "—";
        };
    }
}