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
import org.example.repository.facturacion.TicketRepository;
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


    /** Cuánto ocupa una mesa una reserva: dos reservas se pisan si empiezan a menos de este tiempo. */
    private static final int DURACION_RESERVA_MINUTOS = 90;
    private static final LocalTime FIN_DEL_DIA = LocalTime.of(23, 59, 59);
    /** Parte de cada zona que se puede reservar con anticipación; el resto queda para los que llegan sin reserva. */
    private static final int PORCENTAJE_PARA_RESERVAS = 50;
    @Value("${reservas.monto-senia:30000}")
    private BigDecimal montoSenia;

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final ZonaRepository zonaRepository;
    private final RestauranteRepository restauranteRepository;
    private final ClienteService clienteService;
    private final TicketRepository ticketRepository;

    public ReservaService(ReservaRepository reservaRepository, ClienteRepository clienteRepository,
                          ZonaRepository zonaRepository, RestauranteRepository restauranteRepository,
                          ClienteService clienteService, TicketRepository ticketRepository) {
        this.reservaRepository = reservaRepository;
        this.clienteRepository = clienteRepository;
        this.zonaRepository = zonaRepository;
        this.restauranteRepository = restauranteRepository;
        this.clienteService = clienteService;
        this.ticketRepository = ticketRepository;
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
        // primero lo general (abierto a esa hora, zona activa, lugar en total) y después el cupo de reservas
        validarDisponibilidad(restaurante, zona, reserva.getCantComensales(),
                reserva.getFechaReserva(), reserva.getHoraReserva());
        validarCupoDeReservas(restaurante, zona, reserva.getCantComensales(),
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

    /**
     * Solo se cancela una reserva PENDIENTE o CONFIRMADA, y si todavía no se abrió una mesa con ella:
     * si el cliente ya vino (asistió o tiene la mesa abierta) o se marcó que no vino, ya no se cancela.
     */
    @Transactional
    public Reserva cancelar(int nroReserva) {
        Reserva reserva = buscarPorId(nroReserva);
        switch (reserva.getEstado()) {
            case CANCELADA -> throw new IllegalStateException("La reserva ya esta cancelada");
            case FINALIZADA -> throw new IllegalStateException("El cliente ya asistio: la reserva no se puede cancelar");
            case AUSENTE -> throw new IllegalStateException("Se marco que el cliente no asistio: la reserva no se puede cancelar");
            default -> { }
        }
        if (ticketRepository.existsByReserva_NroReserva(nroReserva)) {
            throw new IllegalStateException("Ya se abrio una mesa con esta reserva: no se puede cancelar");
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
        // si no vino, se le cobra la seña (si vino, no se cobra)
        if (!asistio && reserva.getSenia() != null) {
            reserva.getSenia().cobraPorInasistencia();
        }
        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva reservarWalkIn(ReservaWalkInDTO dto) {
        Restaurante restaurante = restauranteRepository.findById(dto.getNroRestaurante())
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));
        Zona zona = zonaRepository.findById(dto.getIdZona())
                .orElseThrow(() -> new IllegalArgumentException("Zona no encontrada"));

        // el walk-in es de hoy: ahora o más tarde (con unos minutos de margen, por si el horario
        // elegido pasó mientras se cargaban los datos)
        LocalDate fecha = LocalDate.now();
        LocalTime ahora = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalTime hora = dto.getHora() == null ? ahora : dto.getHora().truncatedTo(ChronoUnit.MINUTES);
        LocalTime margen = ahora.minusMinutes(15);
        if (!margen.isAfter(ahora) && hora.isBefore(margen)) {
            throw new IllegalArgumentException("Ese horario ya paso: elegi uno de mas tarde");
        }
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

        if (comensalesOcupados(zona.getIdZona(), fecha, hora) + comensales > zona.getCapacidadMaxima()) {
            throw new IllegalStateException("No hay lugar en esa zona para ese horario");
        }
    }

    /**
     * Comensales que ocupan la zona a esa hora: las reservas que empiezan a menos de DURACION_RESERVA_MINUTOS
     * antes o después (con 1 h 30 min: una de las 11:30 ocupa hasta las 12:59; a las 13:00 ya está libre),
     * sin las canceladas, las ausentes ni las mesas ya cobradas. La ventana no pasa de un día al otro.
     */
    public long comensalesOcupados(int idZona, LocalDate fecha, LocalTime hora) {
        LocalTime[] ventana = ventana(hora);
        return reservaRepository.sumarComensales(idZona, fecha, ventana[0], ventana[1], EstadoReserva.CANCELADA);
    }

    /** Cuántos lugares de la zona se pueden reservar con anticipación (la mitad; el resto es para walk-in). */
    public static int cupoParaReservas(Zona zona) {
        return zona.getCapacidadMaxima() * PORCENTAJE_PARA_RESERVAS / 100;
    }

    /**
     * Una reserva con anticipación solo puede usar el cupo para reservas de la zona. Si no alcanza,
     * se le pide que se comunique con el restaurante (los walk-in sí usan toda la capacidad).
     */
    private void validarCupoDeReservas(Restaurante restaurante, Zona zona, int comensales,
                                       LocalDate fecha, LocalTime hora) {
        if (fecha == null || hora == null) {
            return;   // los datos que faltan los avisa crear()
        }
        int cupo = cupoParaReservas(zona);
        String contacto = restaurante.getTelefono() == null || restaurante.getTelefono().isBlank()
                ? "comunicate con el restaurante"
                : "comunicate con el restaurante al " + restaurante.getTelefono();
        if (comensales > cupo) {
            throw new IllegalStateException("Por la web se reservan hasta " + cupo + " lugares en " + zona.getNombre()
                    + ". Para un grupo mas grande, " + contacto);
        }
        LocalTime[] ventana = ventana(hora);
        long reservados = reservaRepository.sumarComensalesReservados(zona.getIdZona(), fecha, ventana[0], ventana[1],
                EstadoReserva.CANCELADA);
        if (reservados + comensales > cupo) {
            throw new IllegalStateException("Ya no quedan lugares para reservar en " + zona.getNombre()
                    + " en ese horario. Si necesitas mas lugar, " + contacto);
        }
    }

    /**
     * Los horarios del día (cada 15 minutos) en los que ya no entra una reserva de esa cantidad de personas
     * en la zona: con las mismas reglas que al guardarla (cupo para reservas y capacidad total).
     * Lee las reservas del día una sola vez y cuenta en memoria, para no hacer una consulta por horario.
     */
    public List<LocalTime> horariosSinLugarParaReservar(int idZona, LocalDate fecha, int comensales) {
        Zona zona = zonaRepository.findById(idZona)
                .orElseThrow(() -> new IllegalArgumentException("Zona no encontrada"));
        List<Reserva> delDia = reservaRepository.reservasQueOcupan(idZona, fecha, EstadoReserva.CANCELADA);
        int cupo = cupoParaReservas(zona);
        List<LocalTime> sinLugar = new java.util.ArrayList<>();
        for (int minuto = 0; minuto < 24 * 60; minuto += 15) {
            LocalTime hora = LocalTime.of(minuto / 60, minuto % 60);
            LocalTime[] ventana = ventana(hora);
            int total = 0;
            int reservados = 0;
            for (Reserva r : delDia) {
                LocalTime h = r.getHoraReserva();
                if (h != null && !h.isBefore(ventana[0]) && h.isBefore(ventana[1])) {
                    total += r.getCantComensales();
                    if (r.getSenia() != null) {   // los walk-in no tienen seña y no cuentan para el cupo
                        reservados += r.getCantComensales();
                    }
                }
            }
            if (reservados + comensales > cupo || total + comensales > zona.getCapacidadMaxima()) {
                sinLugar.add(hora);
            }
        }
        return sinLugar;
    }

    /** Desde y hasta qué hora se cuentan las reservas que se cruzan con esa hora (sin pasar de un día al otro). */
    private static LocalTime[] ventana(LocalTime hora) {
        LocalTime desde = hora.minusMinutes(DURACION_RESERVA_MINUTOS);
        desde = desde.isAfter(hora) ? LocalTime.MIN : desde.plusMinutes(1);
        LocalTime hasta = hora.plusMinutes(DURACION_RESERVA_MINUTOS);
        // LocalTime.MAX no sirve: MySQL lo guarda como 00:00:00 y la consulta no encontraba nada
        hasta = hasta.isBefore(hora) ? FIN_DEL_DIA : hasta;
        return new LocalTime[]{desde, hasta};
    }
}