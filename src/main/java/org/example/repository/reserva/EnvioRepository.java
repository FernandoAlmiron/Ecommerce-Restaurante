package org.example.repository.reserva;

import org.example.Modelo.reserva.Envio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnvioRepository extends JpaRepository<Envio, Integer> {
    Optional<Envio> findByTicket_NroTicket(int nroTicket);
}
