package org.example.service.reserva;

import org.example.Modelo.enums.EstadoReserva;
import org.example.Modelo.reserva.Reserva;
import org.example.dto.ReservaWalkInDTO;
import org.example.repository.reserva.ReservaRepository;
import org.example.Modelo.persona.Cliente;
import org.example.Modelo.Restaurante;
import org.example.repository.persona.ClienteRepository;
import org.example.repository.reserva.ZonaRepository;
import org.example.repository.RestauranteRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final ZonaRepository zonaRepository;
    private final RestauranteRepository restauranteRepository;

    public ReservaService(ReservaRepository reservaRepository, ClienteRepository clienteRepository,
                          ZonaRepository zonaRepository, RestauranteRepository restauranteRepository) {
        this.reservaRepository = reservaRepository;
        this.clienteRepository = clienteRepository;
        this.zonaRepository = zonaRepository;
        this.restauranteRepository = restauranteRepository;
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
    public Reserva reservarWalkIn(ReservaWalkInDTO dto) {
        Restaurante restaurante = restauranteRepository.findById(dto.getNroRestaurante()).orElseThrow();

        Cliente cliente;
        if (dto.getIdCliente() != null) {
            cliente = clienteRepository.findById(dto.getIdCliente()).orElseThrow();
        } else {
            Cliente nuevo = new Cliente();
            nuevo.setNombre(dto.getNombre());
            nuevo.setApellido(dto.getApellido());
            nuevo.setCelular(dto.getCelular());
            nuevo.setRestaurante(restaurante);
            cliente = clienteRepository.save(nuevo);
        }

        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setZona(zonaRepository.findById(dto.getIdZona()).orElseThrow());
        reserva.setRestaurante(restaurante);
        reserva.setCantComensales(dto.getCantComensales());
        reserva.setFechaReserva(LocalDate.now());
        reserva.setHoraReserva(LocalTime.now());
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        return reservaRepository.save(reserva);
    }
}
