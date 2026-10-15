package org.example.escritorio.vista.caja;

import org.example.Modelo.facturacion.CierreCaja;

import java.math.BigDecimal;

/**
 * Un cierre de caja con lo cobrado ese día en salón por tarjeta y QR.
 * El efectivo esperado lo guarda el cierre; tarjeta y QR se suman de las facturaciones pagadas de esa fecha.
 */
public record CierreFila(CierreCaja cierre, BigDecimal tarjeta, BigDecimal qr) {

    /** Todo lo cobrado en salón ese día: efectivo esperado + tarjeta + QR. */
    public BigDecimal totalDelDia() {
        return cierre.getMontoEsperado().add(tarjeta).add(qr);
    }
}
