package org.example.escritorio.controlador.recepcion;

import org.example.Modelo.Restaurante;
import org.example.Modelo.persona.Cliente;
import org.example.Modelo.reserva.Zona;
import org.example.dto.ReservaResponseDTO;
import org.example.dto.ReservaWalkInDTO;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.comun.Tarea;
import org.example.escritorio.vista.recepcion.WalkInDialog;
import org.example.repository.persona.ClienteRepository;
import org.example.service.RestauranteService;
import org.example.service.reserva.ReservaService;
import org.example.service.reserva.ZonaService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lógica del walk-in (RF-17). La fecha (hoy), el estado CONFIRMADA y el control de horario y cupo
 * los pone ReservaService.reservarWalkIn del backend, no la pantalla. La hora es la elegida
 * (en Recepción se puede elegir más tarde, hoy) o, si no se eligió, ahora.
 */
public class WalkInControlador {

    private final WalkInDialog vista;
    private final ReservaService reservaService;
    private final ZonaService zonaService;
    private final ClienteRepository clienteRepository;
    private final RestauranteService restauranteService;

    /** true cuando ya se buscó el DNI escrito; si cambia el DNI hay que volver a buscar. */
    private boolean dniBuscado;
    /** El cliente encontrado, o null si es nuevo. */
    private Cliente clienteExistente;

    public WalkInControlador(WalkInDialog vista) {
        this.vista = vista;
        this.reservaService = Contexto.get(ReservaService.class);
        this.zonaService = Contexto.get(ZonaService.class);
        this.clienteRepository = Contexto.get(ClienteRepository.class);
        this.restauranteService = Contexto.get(RestauranteService.class);
    }

    /** Los horarios que quedan hoy, cada 15 minutos, mientras el restaurante está abierto. */
    public void cargarHorarios() {
        int nroRestaurante = Sesion.nroRestaurante();
        Tarea.ejecutar(vista.getComboHorario(),
                () -> horariosDeHoy(restauranteService.buscarPorId(nroRestaurante), LocalTime.now()),
                vista::mostrarHorarios);
    }

    /** Desde el próximo cuarto de hora hasta antes del cierre (sin pasar de la medianoche). */
    static List<LocalTime> horariosDeHoy(Restaurante restaurante, LocalTime ahora) {
        List<LocalTime> horarios = new ArrayList<>();
        int desde = (ahora.getHour() * 60 + ahora.getMinute()) / 15 * 15 + 15;
        for (int minuto = desde; minuto < 24 * 60; minuto += 15) {
            LocalTime hora = LocalTime.of(minuto / 60, minuto % 60);
            if (restaurante.estaAbierto(hora) && !hora.equals(restaurante.getHorarioCierre())) {
                horarios.add(hora);
            }
        }
        return horarios;
    }

    /** Las zonas y cuántos lugares libres tiene cada una en el horario elegido. */
    private record Zonas(List<Zona> zonas, Map<Integer, Integer> libres) {
    }

    /** Solo las zonas activas del restaurante del empleado, con los lugares que les quedan libres. */
    public void cargarZonas() {
        int restaurante = Sesion.nroRestaurante();
        LocalTime hora = vista.getHora() != null ? vista.getHora() : LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        Tarea.ejecutar(vista.getComboZona(), () -> {
            List<Zona> zonas = zonaService.listarActivas(restaurante);
            // la misma cuenta que usa el backend al registrar; es solo para mostrar, el que decide es el backend
            Map<Integer, Integer> libres = zonas.stream().collect(Collectors.toMap(Zona::getIdZona,
                    z -> Math.max(0, z.getCapacidadMaxima()
                            - (int) reservaService.comensalesOcupados(z.getIdZona(), LocalDate.now(), hora))));
            return new Zonas(zonas, libres);
        }, datos -> vista.mostrarZonas(datos.zonas(), datos.libres()));
    }

    public void buscarCliente() {
        Integer dni = leerDni();
        if (dni == null) {
            vista.mostrarError("El DNI tiene que tener 7 u 8 números, sin puntos.");
            return;
        }
        Tarea.ejecutar(vista.getBotonBuscar(), () -> clienteRepository.findFirstByDni(dni), encontrado -> {
            dniBuscado = true;
            clienteExistente = encontrado.orElse(null);
            if (clienteExistente != null) {
                vista.mostrarClienteExistente(clienteExistente);
            } else {
                vista.prepararClienteNuevo();
            }
            vista.habilitarRegistrar(true);
        });
    }

    public void alCambiarDni() {
        if (dniBuscado) {
            dniBuscado = false;
            clienteExistente = null;
            vista.limpiarCliente();
        }
    }

    public void registrar() {
        if (!dniBuscado) {
            vista.mostrarError("Primero buscá el cliente por DNI.");
            return;
        }
        Zona zona = vista.getZonaSeleccionada();
        if (zona == null) {
            vista.mostrarError("Elegí una zona.");
            return;
        }
        if (clienteExistente == null && (vista.getNombre().isEmpty() || vista.getApellido().isEmpty())) {
            vista.mostrarError("Completá nombre y apellido del cliente.");
            return;
        }

        ReservaWalkInDTO datos = new ReservaWalkInDTO();
        datos.setNroRestaurante(Sesion.nroRestaurante());
        datos.setIdZona(zona.getIdZona());
        datos.setCantComensales(vista.getComensales());
        datos.setHora(vista.getHora()); // null = ahora
        datos.setDni(leerDni());
        if (clienteExistente != null) {
            datos.setIdCliente(clienteExistente.getIdCliente());
        } else {
            datos.setNombre(vista.getNombre());
            datos.setApellido(vista.getApellido());
            datos.setCelular(vista.getCelular().isEmpty() ? null : vista.getCelular());
        }

        Tarea.ejecutar(vista.getBotonRegistrar(),
                () -> ReservaResponseDTO.desde(reservaService.reservarWalkIn(datos)),
                vista::cerrarCon);
    }

    /** Valida solo el formato; si el cliente existe o no lo resuelve el backend. */
    private Integer leerDni() {
        String texto = vista.getDniTexto();
        if (!texto.matches("\\d{7,8}")) {
            return null;
        }
        return Integer.valueOf(texto);
    }
}