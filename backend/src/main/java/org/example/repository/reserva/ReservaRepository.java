package org.example.repository.reserva;

import org.example.Modelo.enums.EstadoReserva;
import org.example.Modelo.reserva.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    List<Reserva> findByCliente_IdCliente(int idCliente);

    @Query("SELECT COALESCE(SUM(r.cantComensales), 0) FROM Reserva r " +
            "WHERE r.zona.idZona = :idZona AND r.fechaReserva = :fecha " +
            "AND r.horaReserva >= :desde AND r.horaReserva < :hasta " +
            "AND r.estado <> :excluido " +
            // el que no vino no ocupa lugar
            "AND r.estado <> org.example.Modelo.enums.EstadoReserva.AUSENTE " +
            // la mesa ya se cobró (pago confirmado): la gente se fue y el lugar vuelve a estar libre
            "AND NOT EXISTS (SELECT f FROM Facturacion f WHERE f.ticket.reserva = r " +
            "AND f.estadoPago = org.example.Modelo.enums.EstadoPago.PAGADO)")
    Long sumarComensales(@Param("idZona") int idZona,
                         @Param("fecha") LocalDate fecha,
                         @Param("desde") LocalTime desde,
                         @Param("hasta") LocalTime hasta,
                         @Param("excluido") EstadoReserva excluido);

    /**
     * Como sumarComensales, pero solo las reservas hechas con anticipación (las que tienen seña):
     * los walk-in no tienen seña y no cuentan para el cupo de reservas.
     */
    @Query("SELECT COALESCE(SUM(r.cantComensales), 0) FROM Reserva r " +
            "WHERE r.zona.idZona = :idZona AND r.fechaReserva = :fecha " +
            "AND r.horaReserva >= :desde AND r.horaReserva < :hasta " +
            "AND r.estado <> :excluido " +
            "AND r.estado <> org.example.Modelo.enums.EstadoReserva.AUSENTE " +
            "AND EXISTS (SELECT s FROM Senia s WHERE s.reserva = r) " +
            "AND NOT EXISTS (SELECT f FROM Facturacion f WHERE f.ticket.reserva = r " +
            "AND f.estadoPago = org.example.Modelo.enums.EstadoPago.PAGADO)")
    Long sumarComensalesReservados(@Param("idZona") int idZona,
                                   @Param("fecha") LocalDate fecha,
                                   @Param("desde") LocalTime desde,
                                   @Param("hasta") LocalTime hasta,
                                   @Param("excluido") EstadoReserva excluido);

    /** Las reservas de la zona y el día que ocupan lugar (las mismas que suman las consultas de arriba). */
    @Query("SELECT r FROM Reserva r " +
            "WHERE r.zona.idZona = :idZona AND r.fechaReserva = :fecha " +
            "AND r.estado <> :excluido " +
            "AND r.estado <> org.example.Modelo.enums.EstadoReserva.AUSENTE " +
            "AND NOT EXISTS (SELECT f FROM Facturacion f WHERE f.ticket.reserva = r " +
            "AND f.estadoPago = org.example.Modelo.enums.EstadoPago.PAGADO)")
    List<Reserva> reservasQueOcupan(@Param("idZona") int idZona,
                                    @Param("fecha") LocalDate fecha,
                                    @Param("excluido") EstadoReserva excluido);
}
