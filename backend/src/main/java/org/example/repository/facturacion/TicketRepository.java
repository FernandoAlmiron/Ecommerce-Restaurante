package org.example.repository.facturacion;

import org.example.Modelo.facturacion.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
}
