package org.example.escritorio.controlador.recepcion;

import org.example.Modelo.enums.EstadoReserva;
import org.example.dto.ReservaResponseDTO;
import org.example.escritorio.Contexto;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.comun.Tarea;
import org.example.escritorio.vista.recepcion.ReservasPanel;
import org.example.escritorio.vista.recepcion.WalkInDialog;
import org.example.service.reserva.ReservaService;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.util.concurrent.Callable;

/**
 * Lógica de la pantalla de Recepción. Usa el ReservaService del backend (RF-13 a RF-18):
 * las reglas (solo se confirma una PENDIENTE, cupo, etc.) están allá, no acá.
 */
public class ReservasControlador {

    private final ReservasPanel vista;
    private final ReservaService reservaService;

    public ReservasControlador(ReservasPanel vista) {
        this.vista = vista;
        this.reservaService = Contexto.get(ReservaService.class);
    }

    public void cargar() {
        Tarea.ejecutar(vista.getBotonActualizar(),
                () -> reservaService.listarTodos().stream().map(ReservaResponseDTO::desde).toList(),
                reservas -> {
                    vista.mostrarReservas(reservas);
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
        boolean cancelar = r.estado() != EstadoReserva.CANCELADA;
        boolean asistencia = r.estado() == EstadoReserva.CONFIRMADA && r.senia() != null;
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

    public void marcarAsistencia(boolean asistio) {
        JButton boton = asistio ? vista.getBotonAsistio() : vista.getBotonNoAsistio();
        vista.reservaSeleccionada().ifPresent(r -> ejecutar(boton,
                () -> ReservaResponseDTO.desde(reservaService.marcarAsistencia(r.nroReserva(), asistio))));
    }

    public void nuevoWalkIn() {
        WalkInDialog dialogo = new WalkInDialog(SwingUtilities.getWindowAncestor(vista));
        dialogo.setVisible(true); // es modal: sigue cuando se cierra
        dialogo.getReservaCreada().ifPresent(r -> {
            cargar();
            vista.mostrarInfo("Walk-in registrado: reserva N° " + r.nroReserva() + " para " + r.cliente()
                    + " en " + r.zona() + ".");
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