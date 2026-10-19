package org.example.escritorio.vista.caja;

import org.example.Modelo.enums.EstadoEnvio;
import org.example.Modelo.enums.EstadoPago;
import org.example.Modelo.enums.MetodoPago;
import org.example.Modelo.enums.OrigenTicket;
import org.example.Modelo.facturacion.Facturacion;
import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.menu.Pedido;
import org.example.Modelo.reserva.Envio;

import java.math.BigDecimal;
import java.util.List;

/**
 * Una fila de la pantalla de cobros: un ticket con su facturación, si ya tiene.
 * Se arma acá porque el backend no tiene un DTO que junte ticket, facturación y envío.
 */
public record CobroFila(
        int nroTicket,
        OrigenTicket origen,
        String cliente,
        int cantidadPedidos,
        BigDecimal consumo,
        Integer nroFacturacion,
        Integer porcentajePropina,
        BigDecimal propina,
        BigDecimal aPagar,
        MetodoPago metodoPago,
        MetodoPago metodoElegidoPorCliente,
        boolean envioCancelado,
        String estado
) {
    public static final String SIN_FACTURAR = "Sin facturar";
    public static final String PENDIENTE = "Pendiente de pago";
    public static final String PAGADO = "Pagado";

    public boolean sinFacturar() { return SIN_FACTURAR.equals(estado); }
    public boolean pendiente() { return PENDIENTE.equals(estado); }
    public boolean pagado() { return PAGADO.equals(estado); }

    /** Se puede facturar si todavía no tiene facturación, tiene pedidos y el envío no está cancelado. */
    public boolean sePuedeFacturar() {
        return sinFacturar() && cantidadPedidos > 0 && !envioCancelado;
    }

    /** Lo que caja tiene que resolver: tickets para facturar y facturaciones sin pagar. */
    public boolean porCobrar() {
        return sePuedeFacturar() || pendiente();
    }

    /**
     * Llamarlo dentro de Transaccion.leer(...): recorre los pedidos del ticket, que son lazy.
     * @param envio el envío del ticket si es delivery, o null
     */
    public static CobroFila desde(Ticket ticket, Envio envio) {
        List<Pedido> pedidos = ticket.getPedidos() == null ? List.of() : ticket.getPedidos();
        BigDecimal consumo = pedidos.isEmpty() ? BigDecimal.ZERO : ticket.calcularTotal();
        Facturacion f = ticket.getFacturacion();

        return new CobroFila(
                ticket.getNroTicket(),
                ticket.getOrigen(),
                cliente(ticket, envio),
                pedidos.size(),
                f != null ? f.getMontoTotal() : consumo,
                f != null ? f.getNroFacturacion() : null,
                f != null ? f.getPorcentajePropina() : null,
                f != null ? f.getPropina() : BigDecimal.ZERO,
                f != null ? f.getMontoAPagar() : consumo,
                f != null ? f.getMetodoPago() : null,
                envio != null ? envio.getMetodoPago() : null,
                envio != null && envio.getEstado() == EstadoEnvio.CANCELADO,
                f == null ? SIN_FACTURAR : (f.getEstadoPago() == EstadoPago.PAGADO ? PAGADO : PENDIENTE));
    }

    private static String cliente(Ticket ticket, Envio envio) {
        if (envio != null) {
            String receptor = envio.getNombreReceptor() != null ? envio.getNombreReceptor()
                    : envio.getCliente().getNombre() + " " + envio.getCliente().getApellido();
            return envio.getEstado() == EstadoEnvio.CANCELADO ? receptor + " (envío cancelado)" : receptor;
        }
        if (ticket.getReserva() != null) {
            return ticket.getReserva().getCliente().getNombre() + " " + ticket.getReserva().getCliente().getApellido();
        }
        return ticket.getOrigen() == OrigenTicket.DELIVERY ? "Delivery" : "Mesa sin reserva";
    }
}