package org.example.dto;

import org.example.Modelo.enums.EstadoEnvio;
import org.example.Modelo.reserva.Envio;
import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.menu.Pedido;
import java.math.BigDecimal;
import java.util.List;

// Ticket de envio: lo que se entrega al cliente, con sus datos y el total
public record RemitoDTO(
        int idEnvio,
        int nroTicket,
        String cliente,
        String celular,
        String direccionEntrega,
        String nombreReceptor,
        List<Item> pedidos,
        BigDecimal total,
        String estadoPago,
        EstadoEnvio estado,
        String repartidor
) {
    public record Item(String plato, int cantidad, String observaciones) {}

    public static RemitoDTO desde(Envio e) {
        Ticket t = e.getTicket();
        List<Pedido> pedidos = t.getPedidos() == null ? List.of() : t.getPedidos();
        // si ya se facturo, el total incluye la propina; si no, es la suma de los pedidos
        BigDecimal total = t.getFacturacion() != null ? t.getFacturacion().getMontoAPagar() : t.calcularTotal();
        String estadoPago = t.getFacturacion() != null ? t.getFacturacion().getEstadoPago().name() : "SIN FACTURAR";
        return new RemitoDTO(
                e.getIdEnvio(),
                t.getNroTicket(),
                e.getCliente().getNombre() + " " + e.getCliente().getApellido(),
                e.getCliente().getCelular(),
                e.getDireccionEntrega(),
                e.getNombreReceptor(),
                pedidos.stream()
                        .map(p -> new Item(p.getMenu().getNombre(), p.getCantidad(), p.getObservaciones()))
                        .toList(),
                total,
                estadoPago,
                e.getEstado(),
                e.getRepartidor() != null
                        ? e.getRepartidor().getNombre() + " " + e.getRepartidor().getApellido()
                        : "Sin asignar"
        );
    }
}
