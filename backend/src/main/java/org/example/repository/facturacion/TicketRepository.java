package org.example.repository.facturacion;

import org.example.Modelo.facturacion.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    /** Si ya se abrió una mesa (ticket) con esa reserva. */
    boolean existsByReserva_NroReserva(int nroReserva);

    /** Los números de reserva que ya tienen una mesa abierta (o cobrada). */
    @Query("SELECT t.reserva.nroReserva FROM Ticket t WHERE t.reserva IS NOT NULL")
    List<Integer> reservasConMesa();
}
