package org.example.escritorio.vista.caja;

import org.example.Modelo.enums.MetodoPago;
import org.example.Modelo.facturacion.CierreCaja;
import org.example.escritorio.vista.comun.DialogoPapel;
import org.example.escritorio.vista.comun.Formatos;

import java.awt.Window;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Detalle de un cierre de caja ya hecho, como el papel que se imprime al cerrar: el arqueo del efectivo,
 * lo cobrado con tarjeta y QR, y los cobros de salón de ese día uno por uno.
 */
public class CierreDetalleDialog extends DialogoPapel {

    private static final Locale ARGENTINA = Locale.forLanguageTag("es-AR");
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", ARGENTINA);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    /** Un cobro de salón del día del cierre. */
    public record Cobro(int nroTicket, LocalDateTime pagado, MetodoPago metodo, BigDecimal monto) {
    }

    private final CierreFila fila;
    private final List<Cobro> cobros;

    public CierreDetalleDialog(Window padre, CierreFila fila, List<Cobro> cobros) {
        super(padre, "Caja · Cierre N° " + fila.cierre().getIdCierre(), "Cierre de caja",
                DIA.format(fila.cierre().getFecha()));
        this.fila = fila;
        this.cobros = cobros;
        armarPapel();
    }

    @Override
    protected String armarTexto() {
        CierreCaja c = fila.cierre();
        StringBuilder sb = new StringBuilder();
        centrado(sb, "CIERRE DE CAJA N° " + c.getIdCierre());
        centrado(sb, Formatos.fecha(c.getFecha()));
        if (c.getCajera() != null) {
            centrado(sb, "Cajera: " + c.getCajera().getNombre() + " " + c.getCajera().getApellido());
        }
        linea(sb);
        sb.append("EFECTIVO\n");
        columnas(sb, "  Cobrado (esperado)", Formatos.pesos(c.getMontoEsperado()));
        columnas(sb, "  Contado", Formatos.pesos(c.getMontoContado()));
        BigDecimal d = c.getDiferencia() == null ? BigDecimal.ZERO : c.getDiferencia();
        columnas(sb, "  Diferencia", d.signum() == 0 ? "Cuadra"
                : (d.signum() > 0 ? "Sobran " : "Faltan ") + Formatos.pesos(d.abs()));
        linea(sb);
        columnas(sb, "Tarjeta", Formatos.pesos(fila.tarjeta()));
        columnas(sb, "QR", Formatos.pesos(fila.qr()));
        columnas(sb, "TOTAL DEL DIA", Formatos.pesos(fila.totalDelDia()));
        if (c.getObservaciones() != null && !c.getObservaciones().isBlank()) {
            linea(sb);
            sb.append("Observaciones:\n").append(c.getObservaciones()).append('\n');
        }
        linea(sb);
        sb.append("COBROS DE SALON DEL DIA (").append(cobros.size()).append(")\n");
        if (cobros.isEmpty()) {
            sb.append("  Ninguno.\n");
        }
        for (Cobro cobro : cobros) {
            String izquierda = String.format("  #T-%-5d %s  %s", cobro.nroTicket(),
                    cobro.pagado() == null ? "--:--" : HORA.format(cobro.pagado()),
                    cobro.metodo() == null ? "—" : CobrosPanel.texto(cobro.metodo()));
            columnas(sb, izquierda, Formatos.pesos(cobro.monto()));
        }
        linea(sb);
        return sb.toString();
    }
}
