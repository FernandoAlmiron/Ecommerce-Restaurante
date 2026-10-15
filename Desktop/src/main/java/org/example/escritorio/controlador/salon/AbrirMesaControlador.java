package org.example.escritorio.controlador.salon;

import org.example.Modelo.Restaurante;
import org.example.Modelo.enums.EstadoReserva;
import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.reserva.Reserva;
import org.example.dto.ReservaResponseDTO;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.comun.Tarea;
import org.example.escritorio.vista.salon.AbrirMesaDialog;
import org.example.service.facturacion.TicketService;
import org.example.service.reserva.ReservaService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lógica de abrir mesa (RF-19). El ticket lo crea TicketService.crear del backend:
 * pone origen SALON y controla que la reserva sea del mismo restaurante.
 */
public class AbrirMesaControlador {

    private final AbrirMesaDialog vista;
    private final TicketService ticketService;
    private final ReservaService reservaService;

    public AbrirMesaControlador(AbrirMesaDialog vista) {
        this.vista = vista;
        this.ticketService = Contexto.get(TicketService.class);
        this.reservaService = Contexto.get(ReservaService.class);
    }

    /** Reservas confirmadas de hoy, del restaurante del empleado, que todavía no tienen ticket. */
    public void cargarReservas() {
        int restaurante = Sesion.nroRestaurante();
        LocalDate hoy = LocalDate.now();

        Tarea.ejecutar(vista.getComboReserva(), () -> {
            Set<Integer> conTicket = ticketService.listarTodos().stream()
                    .filter(t -> t.getReserva() != null)
                    .map(t -> t.getReserva().getNroReserva())
                    .collect(Collectors.toSet());

            return reservaService.listarTodos().stream()
                    .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA)
                    .filter(r -> hoy.equals(r.getFechaReserva()))
                    .filter(r -> r.getRestaurante() != null
                            && r.getRestaurante().getNroRestaurante() == restaurante)
                    .filter(r -> !conTicket.contains(r.getNroReserva()))
                    .sorted(Comparator.comparing(Reserva::getHoraReserva))
                    .map(ReservaResponseDTO::desde)
                    .toList();
        }, vista::mostrarReservas);
    }

    public void alCambiarConReserva() {
        vista.habilitarCombo(vista.isConReserva());
    }

    public void abrir() {
        ReservaResponseDTO elegida = vista.getReservaSeleccionada();
        if (vista.isConReserva() && elegida == null) {
            vista.mostrarError("Elegí la reserva, o destildá \"Viene con reserva\".");
            return;
        }

        // TicketService.crear solo usa los números: el restaurante y la reserva los busca él en la base
        Ticket datos = new Ticket();
        Restaurante restaurante = new Restaurante();
        restaurante.setNroRestaurante(Sesion.nroRestaurante());
        datos.setRestaurante(restaurante);
        if (vista.isConReserva()) {
            Reserva reserva = new Reserva();
            reserva.setNroReserva(elegida.nroReserva());
            datos.setReserva(reserva);
        }

        Tarea.ejecutar(vista.getBotonAbrir(),
                () -> ticketService.crear(datos).getNroTicket(),
                vista::cerrarCon);
    }
}