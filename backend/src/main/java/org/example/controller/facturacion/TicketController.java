package org.example.controller.facturacion;

import org.example.Modelo.facturacion.Ticket;
import org.example.service.facturacion.TicketService;
import org.example.dto.ComandaDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
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

    @PostMapping
    public Ticket crear(@RequestBody Ticket ticket) {
        return ticketService.crear(ticket);
    }
}
