package org.example.controller.facturacion;

import org.example.Modelo.facturacion.Ticket;
import org.example.service.facturacion.TicketService;
import org.example.service.menu.PedidoService;
import org.example.dto.ComandaDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final PedidoService pedidoService;

    public TicketController(TicketService ticketService, PedidoService pedidoService) {
        this.ticketService = ticketService;
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<Ticket> listarTodos() {
        return ticketService.listarTodos();
    }

    @GetMapping("/{id}")
    public Ticket buscarPorId(@PathVariable int id) {
        return ticketService.buscarPorId(id);
    }

    // comanda para la cocina: solo los platos a preparar
    @GetMapping("/{id}/comanda")
    public ComandaDTO comanda(@PathVariable int id) {
        return ComandaDTO.desde(ticketService.buscarPorId(id));
    }

    // la cocina termino platos de la comanda: pasan a listos los pedidos del cuerpo ([idPedido, ...]);
    // sin cuerpo (o lista vacia) pasan a listos todos los pendientes
    @PutMapping("/{id}/comanda-lista")
    public ComandaDTO marcarComandaLista(@PathVariable int id, @RequestBody(required = false) List<Integer> idsPedido) {
        pedidoService.marcarListos(id, idsPedido);
        return ComandaDTO.desde(ticketService.buscarPorId(id));
    }

    @PostMapping
    public Ticket crear(@RequestBody Ticket ticket) {
        return ticketService.crear(ticket);
    }
}
