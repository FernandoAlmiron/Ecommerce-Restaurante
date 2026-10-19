package org.example.escritorio.vista.salon;

import org.example.Modelo.enums.EstadoPago;
import org.example.Modelo.facturacion.Facturacion;
import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.menu.Pedido;
import org.example.Modelo.reserva.Reserva;

import java.math.BigDecimal;
import java.util.List;

/**
 * Una fila de la tabla de mesas: los datos del ticket que muestra la pantalla de Salón.
 * El backend no tiene un DTO de ticket para listar, por eso se arma acá, solo con datos del ticket.
 */
public record TicketFila(
        int nroTicket,
        Integer nroReserva,
        String cliente,
        String zona,
        Integer comensales,
        int cantidadPedidos,
        BigDecimal total,
        String estado
) {
    public static final String ABIERTA = "Abierta";
    public static final String FACTURADA = "Facturada";
    public static final String PAGADA = "Pagada";

    /** Se puede seguir cargando pedidos mientras el ticket no tenga facturación. */
    public boolean estaAbierta() {
        return ABIERTA.equals(estado);
    }

    /** Llamarlo dentro de Transaccion.leer(...): recorre los pedidos del ticket, que son lazy. */
    public static TicketFila desde(Ticket ticket) {
        Reserva reserva = ticket.getReserva();
        List<Pedido> pedidos = ticket.getPedidos() == null ? List.of() : ticket.getPedidos();
        BigDecimal total = pedidos.isEmpty() ? BigDecimal.ZERO : ticket.calcularTotal();

        return new TicketFila(
                ticket.getNroTicket(),
                reserva != null ? reserva.getNroReserva() : null,
                reserva != null ? reserva.getCliente().getNombre() + " " + reserva.getCliente().getApellido()
                        : "Sin reserva",
                reserva != null ? reserva.getZona().getNombre() : null,
                reserva != null ? reserva.getCantComensales() : null,
                pedidos.size(),
                total,
                estadoDe(ticket.getFacturacion()));
    }

    private static String estadoDe(Facturacion facturacion) {
        if (facturacion == null) {
            return ABIERTA;
        }
        return facturacion.getEstadoPago() == EstadoPago.PAGADO ? PAGADA : FACTURADA;
    }
}