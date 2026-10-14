package org.example.dto;

import org.example.Modelo.enums.OrigenTicket;
import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.menu.Pedido;
import java.util.List;

// Ticket para la cocina: solo lo que hay que preparar, sin precios
public record ComandaDTO(
        int nroTicket,
        OrigenTicket origen,
        Integer nroReserva,
        String zona,
        Integer comensales,
        List<Item> pedidos
) {
    public record Item(String plato, int cantidad, String observaciones) {}

    public static ComandaDTO desde(Ticket t) {
        List<Pedido> pedidos = t.getPedidos() == null ? List.of() : t.getPedidos();
        return new ComandaDTO(
                t.getNroTicket(),
                t.getOrigen(),
                t.getReserva() != null ? t.getReserva().getNroReserva() : null,
                t.getReserva() != null ? t.getReserva().getZona().getNombre() : null,
                t.getReserva() != null ? t.getReserva().getCantComensales() : null,
                pedidos.stream()
                        .map(p -> new Item(p.getMenu().getNombre(), p.getCantidad(), p.getObservaciones()))
                        .toList()
        );
    }
}
