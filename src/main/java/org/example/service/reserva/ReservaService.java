package org.example.service.reserva;

import org.example.Modelo.reserva.Reserva;
import org.example.repository.reserva.ReservaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;

    public ReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public Reserva guardar(Reserva reserva) {
        return reservaRepository.save(reserva);
    }

    public Reserva buscarPorId(int nroReserva) {
        return reservaRepository.findById(nroReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
    }

    public List<Reserva> listarTodos() {
        return reservaRepository.findAll();
    }

    public Reserva confirmar(int nroReserva) {
        Reserva reserva = buscarPorId(nroReserva);
        reserva.confirmar();
        return reservaRepository.save(reserva);
    }

    public Reserva cancelar(int nroReserva) {
        Reserva reserva = buscarPorId(nroReserva);
        reserva.cancelar();
        return reservaRepository.save(reserva);
    }

    public Reserva marcarAsistencia(int nroReserva, boolean asistio) {
        Reserva reserva = buscarPorId(nroReserva);
        reserva.marcarAsistencia(asistio);
        return reservaRepository.save(reserva);
    }
}
