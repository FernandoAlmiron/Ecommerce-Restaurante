package org.example.service.reserva;

import org.example.Modelo.Restaurante;
import org.example.Modelo.enums.EstadoReserva;
import org.example.Modelo.persona.Cliente;
import org.example.Modelo.reserva.Reserva;
import org.example.Modelo.reserva.Senia;
import org.example.Modelo.reserva.Tarjeta;
import org.example.Modelo.reserva.Zona;
import org.example.dto.ReservaWalkInDTO;
import org.example.repository.RestauranteRepository;
import org.example.repository.persona.ClienteRepository;
import org.example.repository.reserva.ReservaRepository;
import org.example.repository.reserva.ZonaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.example.service.persona.ClienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservaService {


    private static final int DURACION_RESERVA_HORAS = 2;
    @Value("${reservas.monto-senia:30000}")
    private BigDecimal montoSenia;

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final ZonaRepository zonaRepository;
    private final RestauranteRepository restauranteRepository;
    private final ClienteService clienteService;

    public ReservaService(ReservaRepository reservaRepository, ClienteRepository clienteRepository,
                          ZonaRepository zonaRepository, RestauranteRepository restauranteRepository,
                          ClienteService clienteService) {
        this.reservaRepository = reservaRepository;
        this.clienteRepository = clienteRepository;
        this.zonaRepository = zonaRepository;
        this.restauranteRepository = restauranteRepository;
        this.clienteService = clienteService;
    }

    public Reserva guardar(Reserva reserva) {
        return reservaRepository.save(reserva);
    }

    public Reserva buscarPorId(int nroReserva) {
        return reservaRepository.findById(nroReserva)
                .orElseThrow(() -> new java.util.NoSuchElementException("Reserva no encontrada"));
    }

    public List<Reserva> listarPorCliente(int idCliente) {
        return reservaRepository.findByCliente_IdCliente(idCliente);
    }

    public List<Reserva> listarTodos() {
        return reservaRepository.findAll();
    }

    @Transactional
    public Reserva crear(Reserva reserva, String usuarioCliente) {
        // cliente con cuenta: se lo toma del login y no tiene que mandar sus datos
        Cliente logueado = usuarioCliente != null ? clienteService.buscarPorUsername(usuarioCliente) : null;
        if (logueado != null) {
            reserva.setCliente(logueado);
            if (reserva.getRestaurante() == null) {
                reserva.setRestaurante(logueado.getRestaurante());
            }
        }
        if (reserva.getCliente() == null || reserva.getZona() == null || reserva.getRestaurante() == null
                || reserva.getFechaReserva() == null || reserva.getHoraReserva() == null) {
            throw new IllegalArgumentException(
                    "Faltan datos: cliente, zona, restaurante, fecha y hora son obligatorios");
        }
        Restaurante restaurante = restauranteRepository.findById(reserva.getRestaurante().getNroRestaurante())
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));
        Zona zona = zonaRepository.findById(reserva.getZona().getIdZona())
                .orElseThrow(() -> new IllegalArgumentException("Zona no encontrada"));
        Cliente datos = reserva.getCliente();
        Cliente cliente;
        if (logueado != null) {
            cliente = logueado;
        } else if (datos.getIdCliente() > 0) {
            cliente = clienteRepository.findById(datos.getIdCliente())
                    .filter(c -> c.getDni() == datos.getDni())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado o el DNI no coincide"));
        } else {
            // lo normal: el cliente solo da su DNI (y sus datos la primera vez)
            cliente = clienteService.obtenerOCrear(datos.getDni(), datos.getNombre(), datos.getApellido(),
                    datos.getCelular(), datos.getEmail(), restaurante);
        }

        if (LocalDateTime.of(reserva.getFechaReserva(), reserva.getHoraReserva()).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha y hora de la reserva ya pasaron");
        }
        validarDisponibilidad(restaurante, zona, reserva.getCantComensales(),
                reserva.getFechaReserva(), reserva.getHoraReserva());
        reserva.setNroReserva(0);
        reserva.setRestaurante(restaurante);
        reserva.setZona(zona);
        reserva.setCliente(cliente);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        Senia senia = reserva.getSenia();
        if (senia == null) {
            throw new IllegalArgumentException("La reserva requiere una seña con tarjeta");
        }
        Tarjeta tarjeta = senia.getTarjeta();
        if (tarjeta == null || !tarjeta.esValida()) {
            throw new IllegalArgumentException("La seña requiere una tarjeta vigente");
        }
        senia.setMonto(montoSenia);
        senia.setIdSenia(0);
        senia.setAsistio(false);
        senia.setReserva(reserva);
        tarjeta.setNroTarjeta(0);
        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva confirmar(int nroReserva) {
        Reserva reserva = buscarPorId(nroReserva);
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException("Solo se puede confirmar una reserva PENDIENTE");
        }
        reserva.confirmar();
        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva cancelar(int nroReserva) {
        Reserva reserva = buscarPorId(nroReserva);
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("La reserva ya esta cancelada");
        }
        reserva.cancelar();
        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva marcarAsistencia(int nroReserva, boolean asistio) {
        Reserva reserva = buscarPorId(nroReserva);
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo se puede registrar asistencia en una reserva CONFIRMADA");
        }
        reserva.marcarAsistencia(asistio);
        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva reservarWalkIn(ReservaWalkInDTO dto) {
        Restaurante restaurante = restauranteRepository.findById(dto.getNroRestaurante())
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));
        Zona zona = zonaRepository.findById(dto.getIdZona())
                .orElseThrow(() -> new IllegalArgumentException("Zona no encontrada"));

        LocalDate fecha = LocalDate.now();
        LocalTime hora = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        validarDisponibilidad(restaurante, zona, dto.getCantComensales(), fecha, hora);

        Cliente cliente;
        if (dto.getIdCliente() != null) {
            cliente = clienteRepository.findById(dto.getIdCliente())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        } else {
            // si ya vino antes, se reutiliza su registro en lugar de duplicarlo
            cliente = clienteService.obtenerOCrear(dto.getDni(), dto.getNombre(), dto.getApellido(),
                    dto.getCelular(), null, restaurante);
        }

        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setZona(zona);
        reserva.setRestaurante(restaurante);
        reserva.setCantComensales(dto.getCantComensales());
        reserva.setFechaReserva(fecha);
        reserva.setHoraReserva(hora);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        return reservaRepository.save(reserva);
    }
    private void validarDisponibilidad(Restaurante restaurante, Zona zona, int comensales,
                                       LocalDate fecha, LocalTime hora) {
        if (comensales <= 0) {
            throw new IllegalArgumentException("La cantidad de comensales debe ser mayor a 0");
        }
        if (zona.getRestaurante() == null
                || zona.getRestaurante().getNroRestaurante() != restaurante.getNroRestaurante()) {
            throw new IllegalArgumentException("La zona no pertenece a ese restaurante");
        }
        if (!restaurante.estaAbierto(hora)) {
            throw new IllegalStateException("El restaurante esta cerrado a esa hora");
        }
        if (!zona.tieneCapacidad(comensales)) {
            throw new IllegalStateException("La zona no esta activa o no admite esa cantidad de comensales");
        }

        LocalTime desde = hora.minusHours(DURACION_RESERVA_HORAS);
        desde = desde.isAfter(hora) ? LocalTime.MIN : desde.plusMinutes(1);
        LocalTime hasta = hora.plusHours(DURACION_RESERVA_HORAS);
        hasta = hasta.isBefore(hora) ? LocalTime.MAX : hasta;

        long ocupados = reservaRepository.sumarComensales(
                zona.getIdZona(), fecha, desde, hasta, EstadoReserva.CANCELADA);
        if (ocupados + comensales > zona.getCapacidadMaxima()) {
            throw new IllegalStateException("No hay lugar en esa zona para ese horario");
        }
    }
}