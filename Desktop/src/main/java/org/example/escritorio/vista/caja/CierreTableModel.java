package org.example.escritorio.vista.caja;

import org.example.Modelo.facturacion.CierreCaja;
import org.example.escritorio.vista.comun.ModeloTablaBase;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Columnas del historial de cierres: el arqueo del efectivo y lo cobrado por tarjeta y QR ese día. */
public class CierreTableModel extends ModeloTablaBase<CierreFila> {

    public static final int COL_NRO = 0;
    public static final int COL_DIFERENCIA = 5;
    public static final int COL_TOTAL = 8;

    public CierreTableModel() {
        super("N°", "Fecha", "Cajera", "Esperado", "Contado", "Diferencia",
                "Tarjeta", "QR", "Total del día", "Observaciones");
    }

    @Override
    protected Object valor(CierreFila fila, int columna) {
        CierreCaja c = fila.cierre();
        return switch (columna) {
            case 0 -> c.getIdCierre();
            case 1 -> c.getFecha();
            case 2 -> c.getCajera() == null ? "" : c.getCajera().getNombre() + " " + c.getCajera().getApellido();
            case 3 -> c.getMontoEsperado();
            case 4 -> c.getMontoContado();
            case 5 -> c.getDiferencia();
            case 6 -> fila.tarjeta();
            case 7 -> fila.qr();
            case 8 -> fila.totalDelDia();
            case 9 -> c.getObservaciones() == null ? "" : c.getObservaciones();
            default -> null;
        };
    }

    @Override
    protected Class<?> tipo(int columna) {
        return switch (columna) {
            case 0 -> Integer.class;
            case 1 -> LocalDate.class;
            case 3, 4, 5, 6, 7, 8 -> BigDecimal.class;
            default -> String.class;
        };
    }
}