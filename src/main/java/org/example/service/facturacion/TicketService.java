package org.example.service.facturacion;

import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.menu.Pedido;
import org.example.repository.facturacion.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket guardar(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public Ticket buscarPorId(int nroTicket) {
        return ticketRepository.findById(nroTicket)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
    }

    public List<Ticket> listarTodos() {
        return ticketRepository.findAll();
    }

    public Ticket agregarPedido(int nroTicket, Pedido pedido) {
        Ticket ticket = buscarPorId(nroTicket);
        ticket.agregarPedido(pedido);
        return ticketRepository.save(ticket);
    }

    public double calcularTotal(int nroTicket) {
        Ticket ticket = buscarPorId(nroTicket);
        return ticket.calcularTotal();
    }
}