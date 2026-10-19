package org.example.escritorio.vista.caja;

import org.example.Modelo.reserva.Tarjeta;
import org.example.escritorio.vista.comun.ModeloTablaBase;

import java.time.format.DateTimeFormatter;

/** Columnas de las tarjetas registradas. Vigente sale de Tarjeta.esValida() del backend. */
public class TarjetaTableModel extends ModeloTablaBase<Tarjeta> {

    public static final int COL_NRO = 0;
    private static final DateTimeFormatter MES_ANIO = DateTimeFormatter.ofPattern("MM/yyyy");

    public TarjetaTableModel() {
        super("N°", "Titular", "Vencimiento", "Vigente");
    }

    @Override
    protected Object valor(Tarjeta t, int columna) {
        return switch (columna) {
            case 0 -> t.getNroTarjeta();
            case 1 -> t.getTitular();
            case 2 -> t.getVencimiento() == null ? "—" : MES_ANIO.format(t.getVencimiento());
            case 3 -> t.esValida();
            default -> null;
        };
    }

    @Override
    protected Class<?> tipo(int columna) {
        return switch (columna) {
            case 0 -> Integer.class;
            case 3 -> Boolean.class;
            default -> String.class;
        };
    }
}