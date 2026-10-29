package org.example.escritorio.controlador.recepcion;

import org.example.Modelo.enums.EstadoReserva;
import org.example.dto.ReservaResponseDTO;
import org.example.escritorio.Contexto;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.comun.Tarea;
import org.example.escritorio.vista.recepcion.ReservasPanel;
import org.example.escritorio.vista.recepcion.WalkInDialog;
import org.example.service.facturacion.TicketService;
import org.example.service.reserva.ReservaService;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Lógica de la pantalla de Recepción. Usa el ReservaService del backend (RF-13 a RF-18):
 * las reglas (solo se confirma una PENDIENTE, cupo, etc.) están allá, no acá.
 */
public class ReservasControlador {

    private final ReservasPanel vista;
    private final ReservaService reservaService;
    private final TicketService ticketService;
    /** Las reservas con las que ya se abrió una mesa: esas no se pueden cancelar. */
    private Set<Integer> conMesa = Set.of();

    public ReservasControlador(ReservasPanel vista) {
        this.vista = vista;
        this.reservaService = Contexto.get(ReservaService.class);
        this.ticketService = Contexto.get(TicketService.class);
    }

    private record Datos(List<ReservaResponseDTO> reservas, Set<Integer> conMesa) {
    }

    public void cargar() {
        Tarea.ejecutar(vista.getBotonActualizar(),
                () -> new Datos(reservaService.listarTodos().stream().map(ReservaResponseDTO::desde).toList(),
                        ticketService.reservasConMesa()),
                datos -> {
                    conMesa = datos.conMesa();
                    vista.setReservasConMesa(conMesa);
                    vista.mostrarReservas(datos.reservas());
                    alCambiarSeleccion();
                });
    }

    /**
     * Habilita solo las acciones que el backend va a aceptar para la reserva elegida,
     * así el usuario no hace clics que terminan en error. El backend las valida igual.
     */
    public void alCambiarSeleccion() {
        ReservaResponseDTO r = vista.reservaSeleccionada().orElse(null);
        if (r == null) {
            vista.habilitarAcciones(false, false, false);
            return;
        }
        boolean confirmar = r.estado() == EstadoReserva.PENDIENTE;
        // como en ReservaService.cancelar: pendiente o confirmada, y sin mesa abierta
        boolean cancelar = (r.estado() == EstadoReserva.PENDIENTE || r.estado() == EstadoReserva.CONFIRMADA)
                && !conMesa.contains(r.nroReserva());
        // también los walk-in (no tienen seña): pueden ser para más tarde y hay que registrar si vinieron
        boolean asistencia = r.estado() == EstadoReserva.CONFIRMADA;
        vista.habilitarAcciones(confirmar, cancelar, asistencia);
    }

    public void confirmar() {
        vista.reservaSeleccionada().ifPresent(r -> ejecutar(vista.getBotonConfirmar(),
                () -> ReservaResponseDTO.desde(reservaService.confirmar(r.nroReserva()))));
    }

    public void cancelar() {
        vista.reservaSeleccionada().ifPresent(r -> {
            String pregunta = "¿Cancelar la reserva N° " + r.nroReserva() + " de " + r.cliente()
                    + " (" + Formatos.fecha(r.fecha()) + " " + Formatos.hora(r.hora()) + ")?";
            if (vista.confirmar(pregunta)) {
                ejecutar(vista.getBotonCancelar(),
                        () -> ReservaResponseDTO.desde(reservaService.cancelar(r.nroReserva())));
            }
        });
    }

    /**
     * Asistió: la seña no se cobra. No asistió: se cobra la seña a la tarjeta (lo hace el backend).
     * Los walk-in no tienen seña: solo queda registrado si vino o no.
     */
    public void marcarAsistencia(boolean asistio) {
        JButton boton = asistio ? vista.getBotonAsistio() : vista.getBotonNoAsistio();
        vista.reservaSeleccionada().ifPresent(r -> {
            boolean conSenia = r.senia() != null;
            if (!asistio && !vista.confirmar("¿Marcar que " + r.cliente() + " no asistió?\n"
                    + (conSenia ? "Se le cobra la seña de " + Formatos.pesos(r.senia()) + " a la tarjeta."
                                : "No tiene seña: no se cobra nada."))) {
                return;
            }
            Tarea.ejecutar(boton,
                    () -> ReservaResponseDTO.desde(reservaService.marcarAsistencia(r.nroReserva(), asistio)),
                    actualizada -> {
                        vista.actualizarReserva(actualizada);
                        alCambiarSeleccion();
                        if (!conSenia) {
                            vista.mostrarInfo(asistio ? "Asistencia registrada." : "Inasistencia registrada.");
                        } else {
                            vista.mostrarInfo(asistio
                                    ? "Asistencia registrada: la seña de " + r.cliente() + " no se cobra."
                                    : "Inasistencia registrada: se cobró la seña de " + Formatos.pesos(r.senia()) + ".");
                        }
                    },
                    mensaje -> {
                        vista.mostrarError(mensaje);
                        cargar();
                    });
        });
    }

    public void nuevoWalkIn() {
        WalkInDialog dialogo = new WalkInDialog(SwingUtilities.getWindowAncestor(vista));
        dialogo.setVisible(true); // es modal: sigue cuando se cierra
        dialogo.getReservaCreada().ifPresent(r -> {
            cargar();
            vista.mostrarInfo("Walk-in registrado: reserva N° " + r.nroReserva() + " para " + r.cliente()
                    + " a las " + Formatos.hora(r.hora()) + " en " + r.zona() + ".");
        });
    }

    /** Ejecuta una acción sobre una reserva: si sale bien actualiza la fila; si falla, avisa y recarga. */
    private void ejecutar(JButton origen, Callable<ReservaResponseDTO> accion) {
        Tarea.ejecutar(origen, accion,
                actualizada -> {
                    vista.actualizarReserva(actualizada);
                    alCambiarSeleccion();
                },
                mensaje -> {
                    vista.mostrarError(mensaje);
                    cargar(); // los datos pueden haber cambiado: se vuelven a pedir
                });
    }
}