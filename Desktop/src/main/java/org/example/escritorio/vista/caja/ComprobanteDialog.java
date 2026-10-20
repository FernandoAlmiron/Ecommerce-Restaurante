package org.example.escritorio.vista.caja;

import org.example.Modelo.enums.EstadoPago;
import org.example.dto.ComprobanteDTO;
import org.example.escritorio.vista.comun.DialogoPapel;
import org.example.escritorio.vista.comun.Formatos;

import java.awt.Window;
import java.time.format.DateTimeFormatter;

/**
 * Comprobante de una facturación (RF-45), armado con el ComprobanteDTO del backend.
 * Se muestra como un ticket de papel y se puede imprimir.
 */
public class ComprobanteDialog extends DialogoPapel {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ComprobanteDTO comprobante;

    public ComprobanteDialog(Window padre, ComprobanteDTO comprobante) {
        super(padre, "Caja · Ticket N° " + comprobante.nroTicket(),
                "Comprobante N° " + comprobante.nroFacturacion(),
                comprobante.estadoPago() == EstadoPago.PAGADO ? "Pagado" : "Pendiente de pago");
        this.comprobante = comprobante;
        armarPapel();
    }

    @Override
    protected String armarTexto() {
        return armarTexto(comprobante);
    }

    private static String armarTexto(ComprobanteDTO c) {
        StringBuilder sb = new StringBuilder();
        centrado(sb, c.restaurante());
        if (c.direccion() != null) {
            centrado(sb, c.direccion());
        }
        linea(sb);
        sb.append("Comprobante N° ").append(c.nroFacturacion())
                .append("   Ticket N° ").append(c.nroTicket()).append('\n');
        if (c.fecha() != null) {
            sb.append("Fecha: ").append(FECHA_HORA.format(c.fecha())).append('\n');
        }
        linea(sb);
        for (ComprobanteDTO.Item item : c.detalle()) {
            sb.append(item.plato()).append('\n');
            columnas(sb, "  " + item.cantidad() + " x " + Formatos.pesos(item.precioUnitario()),
                    Formatos.pesos(item.subtotal()));
        }
        linea(sb);
        columnas(sb, "Subtotal", Formatos.pesos(c.subtotal()));
        if (c.porcentajePropina() != null) {
            columnas(sb, "Propina " + c.porcentajePropina() + " %", Formatos.pesos(c.propina()));
        }
        columnas(sb, "TOTAL", Formatos.pesos(c.total()));
        linea(sb);
        columnas(sb, "Método de pago", c.metodoPago() == null ? "—" : CobrosPanel.texto(c.metodoPago()));
        columnas(sb, "Estado", c.estadoPago() == EstadoPago.PAGADO ? "PAGADO" : "PENDIENTE DE PAGO");
        if (c.fechaPago() != null) {
            columnas(sb, "Pagado el", FECHA_HORA.format(c.fechaPago()));
        }
        linea(sb);
        centrado(sb, "¡Gracias por su visita!");
        return sb.toString();
    }
}
