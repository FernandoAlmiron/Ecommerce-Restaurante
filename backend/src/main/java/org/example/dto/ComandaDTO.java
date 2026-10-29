package org.example.dto;

import org.example.Modelo.enums.OrigenTicket;
import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.menu.Pedido;
import java.util.List;

// Ticket para la cocina: solo lo que hay que preparar (los pedidos que no estan listos), sin precios
public record ComandaDTO(
        int nroTicket,
        OrigenTicket origen,
        Integer nroReserva,
        String zona,
        Integer comensales,
        List<Item> pedidos
) {
    // idPedido: para marcar como listos solo los platos que la cocina ya termino
    public record Item(int idPedido, String plato, int cantidad, String observaciones) {}

    public static ComandaDTO desde(Ticket t) {
        List<Pedido> pedidos = t.getPedidos() == null ? List.of() : t.getPedidos();
        return new ComandaDTO(
                t.getNroTicket(),
                t.getOrigen(),
                t.getReserva() != null ? t.getReserva().getNroReserva() : null,
                t.getReserva() != null ? t.getReserva().getZona().getNombre() : null,
                t.getReserva() != null ? t.getReserva().getCantComensales() : null,
                pedidos.stream()
                        .filter(Pedido::estaEnCocina)   // lo pendiente de la cocina (las bebidas no van)
                        .map(p -> new Item(p.getIdPedido(), p.getMenu().getNombre(), p.getCantidad(), p.getObservaciones()))
                        .toList()
        );
    }
}
