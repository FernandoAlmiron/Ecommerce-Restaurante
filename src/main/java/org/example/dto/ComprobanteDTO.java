package org.example.dto;

import org.example.Modelo.enums.EstadoPago;
import org.example.Modelo.enums.MetodoPago;
import org.example.Modelo.facturacion.Facturacion;
import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.menu.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Ticket de facturacion: detalle con precios, propina y total a pagar
public record ComprobanteDTO(
        int nroFacturacion,
        int nroTicket,
        String restaurante,
        String direccion,
        LocalDateTime fecha,
        List<Item> detalle,
        BigDecimal subtotal,
        Integer porcentajePropina,
        BigDecimal propina,
        BigDecimal total,
        MetodoPago metodoPago,
        EstadoPago estadoPago,
        LocalDateTime fechaPago
) {
    public record Item(String plato, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {}

    public static ComprobanteDTO desde(Facturacion f) {
        Ticket t = f.getTicket();
        List<Pedido> pedidos = t.getPedidos() == null ? List.of() : t.getPedidos();
        return new ComprobanteDTO(
                f.getNroFacturacion(),
                t.getNroTicket(),
                t.getRestaurante().getNombre(),
                t.getRestaurante().getDireccion(),
                f.getFecha(),
                pedidos.stream()
                        .map(p -> new Item(p.getMenu().getNombre(), p.getCantidad(),
                                p.getPrecioUnitario(), p.calcularSubtotal()))
                        .toList(),
                f.getMontoTotal(),
                f.getPorcentajePropina(),
                f.getPropina(),
                f.getMontoAPagar(),
                f.getMetodoPago(),
                f.getEstadoPago(),
                f.getFechaPago()
        );
    }
}
