package org.example.escritorio.controlador.reparto;

import org.example.Modelo.enums.EstadoEnvio;
import org.example.Modelo.reserva.Envio;
import org.example.dto.RemitoDTO;
import org.example.escritorio.Contexto;
import org.example.escritorio.Transaccion;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.comun.Tarea;
import org.example.escritorio.vista.reparto.AsignarRepartidorDialog;
import org.example.escritorio.vista.reparto.RemitoDialog;
import org.example.escritorio.vista.reparto.RepartoPanel;
import org.example.service.reserva.EnvioService;
import org.springframework.security.access.AccessDeniedException;

import javax.swing.SwingUtilities;

/**
 * Lógica de reparto con EnvioService (RF-36 a RF-39). Las transiciones válidas las controla
 * Envio.cambiarEstado del backend; acá solo se habilitan los botones que tienen sentido.
 */
public class RepartoControlador {

    private final RepartoPanel vista;
    private final EnvioService envioService;

    public RepartoControlador(RepartoPanel vista) {
        this.vista = vista;
        this.envioService = Contexto.get(EnvioService.class);
    }

    /**
     * El repartidor ve solo los envíos asignados a él (RF-38); el ADMIN ve todos los del restaurante.
     * @param seleccionar envío a dejar seleccionado, o null para mantener el actual
     */
    public void cargar(Integer seleccionar) {
        Integer aSeleccionar = seleccionar != null ? seleccionar
                : vista.envioSeleccionado().map(RemitoDTO::idEnvio).orElse(null);
        int restaurante = Sesion.nroRestaurante();
        boolean admin = Sesion.esAdmin();
        String usuario = Sesion.usuario();

        Tarea.ejecutar(vista.getBotonActualizar(),
                // RemitoDTO.desde recorre los pedidos del ticket (lazy): va dentro de la transacción
                () -> Transaccion.leer(() -> envioService.listarTodos().stream()
                        .filter(e -> e.getTicket() != null && e.getTicket().getRestaurante() != null
                                && e.getTicket().getRestaurante().getNroRestaurante() == restaurante)
                        .filter(e -> admin || esDe(e, usuario))
                        .map(RemitoDTO::desde)
                        .toList()),
                envios -> {
                    vista.mostrarEnvios(envios);
                    if (aSeleccionar != null) {
                        vista.seleccionarEnvio(aSeleccionar);
                    }
                    alCambiarSeleccion();
                });
    }

    /** Habilita solo lo que Envio.cambiarEstado va a aceptar. El backend lo valida igual. */
    public void alCambiarSeleccion() {
        RemitoDTO r = vista.envioSeleccionado().orElse(null);
        if (r == null) {
            vista.habilitarAcciones(false, false, false, false, false);
            return;
        }
        boolean activo = r.estado() == EstadoEnvio.PENDIENTE || r.estado() == EstadoEnvio.EN_CAMINO;
        boolean conRepartidor = !"Sin asignar".equals(r.repartidor());
        vista.habilitarAcciones(
                activo,
                true,
                r.estado() == EstadoEnvio.PENDIENTE && conRepartidor,
                r.estado() == EstadoEnvio.EN_CAMINO,
                activo);
    }

    /** RF-37: EN_CAMINO, ENTREGADO o CANCELADO. Cancelar pide confirmación. */
    public void cambiarEstado(EstadoEnvio nuevo) {
        vista.envioSeleccionado().ifPresent(r -> {
            if (nuevo == EstadoEnvio.CANCELADO
                    && !vista.confirmar("¿Cancelar el envío N° " + r.idEnvio() + " a " + r.direccionEntrega() + "?")) {
                return;
            }
            String usuario = Sesion.usuario();
            boolean admin = Sesion.esAdmin();
            Tarea.ejecutar(vista.botonPara(nuevo), () -> {
                try {
                    // al entregar, el repartidor cobra: factura, pago y ENTREGADO van juntos o ninguno
                    return nuevo == EstadoEnvio.ENTREGADO
                            ? envioService.entregarYCobrar(r.idEnvio(), usuario, admin)
                            : envioService.actualizarEstado(r.idEnvio(), nuevo, usuario, admin);
                } catch (AccessDeniedException e) {
                    // RF-38: el backend no deja mover un envío de otro repartidor
                    throw new IllegalStateException("Este envío no está asignado a vos.");
                }
            }, actualizado -> cargar(r.idEnvio()), mensaje -> {
                vista.mostrarError(mensaje);
                cargar(r.idEnvio());
            });
        });
    }

    /** RF-39. Los datos ya vienen con la fila: no hace falta volver a la base. */
    public void verRemito() {
        vista.envioSeleccionado().ifPresent(r ->
                new RemitoDialog(SwingUtilities.getWindowAncestor(vista), r).setVisible(true));
    }

    /** RF-36: solo ADMIN ve el botón. */
    public void asignarRepartidor() {
        vista.envioSeleccionado().ifPresent(r -> {
            AsignarRepartidorDialog dialogo = new AsignarRepartidorDialog(SwingUtilities.getWindowAncestor(vista), r);
            dialogo.setVisible(true); // es modal: sigue cuando se cierra
            if (dialogo.seAsigno()) {
                cargar(r.idEnvio());
            }
        });
    }

    private static boolean esDe(Envio envio, String usuario) {
        return envio.getRepartidor() != null && usuario.equals(envio.getRepartidor().getUsername());
    }
}