package org.example.repository.reserva;

import org.example.Modelo.enums.EstadoReserva;
import org.example.Modelo.reserva.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    @Query("SELECT COALESCE(SUM(r.cantComensales), 0) FROM Reserva r " +
            "WHERE r.zona.idZona = :idZona AND r.fechaReserva = :fecha " +
            "AND r.horaReserva >= :desde AND r.horaReserva < :hasta " +
            "AND r.estado <> :excluido")
    Long sumarComensales(@Param("idZona") int idZona,
                         @Param("fecha") LocalDate fecha,
                         @Param("desde") LocalTime desde,
                         @Param("hasta") LocalTime hasta,
                         @Param("excluido") EstadoReserva excluido);
}