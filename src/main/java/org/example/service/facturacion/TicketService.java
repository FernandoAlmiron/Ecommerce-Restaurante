package org.example.service.facturacion;

import org.example.Modelo.Restaurante;
import org.example.Modelo.enums.OrigenTicket;
import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.menu.Pedido;
import org.example.Modelo.reserva.Reserva;
import org.example.repository.RestauranteRepository;
import org.example.repository.facturacion.TicketRepository;
import org.example.repository.reserva.ReservaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final RestauranteRepository restauranteRepository;
    private final ReservaRepository reservaRepository;

    public TicketService(TicketRepository ticketRepository, RestauranteRepository restauranteRepository,
                         ReservaRepository reservaRepository) {
        this.ticketRepository = ticketRepository;
        this.restauranteRepository = restauranteRepository;
        this.reservaRepository = reservaRepository;
    }

    // Un ticket nuevo nace VACIO: de lo que llega solo se toma el restaurante y (opcional) la reserva.
    // Los pedidos se cargan por /api/pedidos y la facturacion por /api/facturaciones,
    // asi no se puede colar un pedido sin descontar stock ni una facturacion ya "pagada".
    public Ticket crear(Ticket datos) {
        if (datos.getRestaurante() == null) {
            throw new IllegalArgumentException("El ticket necesita un restaurante");
        }
        Restaurante restaurante = restauranteRepository.findById(datos.getRestaurante().getNroRestaurante())
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));

        Ticket ticket = new Ticket();
        ticket.setRestaurante(restaurante);
        ticket.setOrigen(OrigenTicket.SALON);   // los de delivery los crea EnvioService

        if (datos.getReserva() != null) {
            Reserva reserva = reservaRepository.findById(datos.getReserva().getNroReserva())
                    .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
            if (reserva.getRestaurante().getNroRestaurante() != restaurante.getNroRestaurante()) {
                throw new IllegalArgumentException("La reserva no pertenece a ese restaurante");
            }
            ticket.setReserva(reserva);
        }
        return ticketRepository.save(ticket);
    }

    public Ticket buscarPorId(int nroTicket) {
        return ticketRepository.findById(nroTicket)
                .orElseThrow(() -> new NoSuchElementException("Ticket no encontrado"));
    }

    public List<Ticket> listarTodos() {
        return ticketRepository.findAll();
    }

    public Ticket agregarPedido(int nroTicket, Pedido pedido) {
        Ticket ticket = buscarPorId(nroTicket);
        ticket.agregarPedido(pedido);
        return ticketRepository.save(ticket);
    }

    public BigDecimal calcularTotal(int nroTicket) {
        Ticket ticket = buscarPorId(nroTicket);
        return ticket.calcularTotal();
    }
}