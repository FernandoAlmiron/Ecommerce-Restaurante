package org.example.escritorio.controlador.salon;

import org.example.Modelo.enums.OrigenTicket;
import org.example.Modelo.menu.Pedido;
import org.example.escritorio.Contexto;
import org.example.escritorio.Transaccion;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.comun.Tarea;
import org.example.escritorio.vista.salon.AbrirMesaDialog;
import org.example.escritorio.vista.salon.PedidoDialog;
import org.example.escritorio.vista.salon.TicketFila;
import org.example.escritorio.vista.salon.TicketsPanel;
import org.example.service.facturacion.TicketService;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de la pantalla de Salón. Usa TicketService del backend; los pedidos se cargan
 * con PedidoService (desde PedidoDialog), que es el que valida disponibilidad y descuenta stock.
 */
public class TicketsControlador {

    private final TicketsPanel vista;
    private final TicketService ticketService;

    public TicketsControlador(TicketsPanel vista) {
        this.vista = vista;
        this.ticketService = Contexto.get(TicketService.class);
    }

    /**
     * Carga los tickets de salón del restaurante del empleado.
     * @param seleccionar nro de ticket a dejar seleccionado después de cargar, o null para mantener el actual
     */
    public void cargar(Integer seleccionar) {
        Integer aSeleccionar = seleccionar != null ? seleccionar
                : vista.ticketSeleccionado().map(TicketFila::nroTicket).orElse(null);
        int restaurante = Sesion.nroRestaurante();

        Tarea.ejecutar(vista.getBotonActualizar(),
                () -> Transaccion.leer(() -> ticketService.listarTodos().stream()
                        .filter(t -> t.getOrigen() == OrigenTicket.SALON)
                        .filter(t -> t.getRestaurante() != null
                                && t.getRestaurante().getNroRestaurante() == restaurante)
                        .map(TicketFila::desde)
                        .toList()),
                tickets -> {
                    vista.mostrarTickets(tickets);
                    if (aSeleccionar != null) {
                        vista.seleccionarTicket(aSeleccionar);
                    }
                    alCambiarSeleccion();
                });
    }

    /** Al elegir una mesa se piden sus pedidos a la base. */
    public void alCambiarSeleccion() {
        TicketFila ticket = vista.ticketSeleccionado().orElse(null);
        if (ticket == null) {
            vista.limpiarPedidos();
            vista.habilitarAgregarPedido(false);
            return;
        }
        vista.habilitarAgregarPedido(ticket.estaAbierta());

        int nro = ticket.nroTicket();
        Tarea.ejecutar(vista.getTablaPedidos(),
                // se copia la lista dentro de la transacción: afuera, la lista lazy ya no se puede leer
                () -> Transaccion.leer(() -> {
                    List<Pedido> pedidos = ticketService.buscarPorId(nro).getPedidos();
                    return pedidos == null ? List.<Pedido>of() : new ArrayList<>(pedidos);
                }),
                pedidos -> {
                    // si mientras tanto eligieron otra mesa, esta respuesta ya no sirve
                    boolean sigueElegido = vista.ticketSeleccionado()
                            .map(t -> t.nroTicket() == nro).orElse(false);
                    if (sigueElegido) {
                        vista.mostrarPedidos(ticket, pedidos);
                    }
                });
    }

    public void abrirMesa() {
        AbrirMesaDialog dialogo = new AbrirMesaDialog(SwingUtilities.getWindowAncestor(vista));
        dialogo.setVisible(true); // es modal: sigue cuando se cierra
        dialogo.getTicketCreado().ifPresent(this::cargar);
    }

    public void agregarPedido() {
        vista.ticketSeleccionado().filter(TicketFila::estaAbierta).ifPresent(ticket -> {
            PedidoDialog dialogo = new PedidoDialog(SwingUtilities.getWindowAncestor(vista), ticket.nroTicket());
            dialogo.setVisible(true);
            if (dialogo.huboCambios()) {
                cargar(ticket.nroTicket());
            }
        });
    }
}