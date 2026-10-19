package org.example.escritorio.controlador.salon;

import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.menu.Menu;
import org.example.Modelo.menu.Pedido;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.comun.Tarea;
import org.example.escritorio.vista.salon.PedidoDialog;
import org.example.service.menu.MenuService;
import org.example.service.menu.PedidoService;

import java.util.Comparator;

/**
 * Lógica de cargar un pedido (RF-20). Se usa PedidoService.guardar del backend, que valida
 * que el plato esté disponible y el ticket no esté facturado, pone el precio del menú y descuenta stock.
 * (No se usa TicketService.agregarPedido porque se saltea esas validaciones.)
 */
public class PedidoControlador {

    private final PedidoDialog vista;
    private final MenuService menuService;
    private final PedidoService pedidoService;

    public PedidoControlador(PedidoDialog vista) {
        this.vista = vista;
        this.menuService = Contexto.get(MenuService.class);
        this.pedidoService = Contexto.get(PedidoService.class);
    }

    /** Solo los platos disponibles del restaurante del empleado, ordenados por nombre. */
    public void cargarMenus() {
        int restaurante = Sesion.nroRestaurante();
        Tarea.ejecutar(vista.getComboMenu(),
                () -> menuService.listarTodos().stream()
                        .filter(Menu::estaDisponible)
                        .filter(m -> m.getRestaurante() != null
                                && m.getRestaurante().getNroRestaurante() == restaurante)
                        .sorted(Comparator.comparing(Menu::getNombre))
                        .toList(),
                vista::mostrarMenus);
    }

    public void agregar() {
        Menu menu = vista.getMenuSeleccionado();
        if (menu == null) {
            vista.mostrarError("Elegí un plato.");
            return;
        }

        // PedidoService.guardar solo usa los ids: el menú y el ticket los busca él en la base
        Ticket ticket = new Ticket();
        ticket.setNroTicket(vista.getNroTicket());

        Pedido pedido = new Pedido();
        pedido.setMenu(menu);
        pedido.setTicket(ticket);
        pedido.setCantidad(vista.getCantidad());
        pedido.setObservaciones(vista.getObservaciones().isEmpty() ? null : vista.getObservaciones());

        String descripcion = pedido.getCantidad() + " × " + menu.getNombre();
        Tarea.ejecutar(vista.getBotonAgregar(),
                () -> pedidoService.guardar(pedido),
                guardado -> vista.pedidoAgregado(descripcion));
    }
}