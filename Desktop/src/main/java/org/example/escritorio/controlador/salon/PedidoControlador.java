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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lógica de cargar pedidos (RF-20). Los platos se juntan primero en una lista de la ventana
 * (se pueden corregir o sacar) y se guardan todos juntos al tocar "Cargar pedido".
 * Se usa PedidoService.guardar del backend, que valida que el plato esté disponible y el ticket
 * no esté facturado, pone el precio del menú y descuenta stock.
 * (No se usa TicketService.agregarPedido porque se saltea esas validaciones.)
 */
public class PedidoControlador {

    /** Un plato de la lista, todavía sin guardar. */
    public record Renglon(Menu menu, int cantidad, String observaciones) {
        public BigDecimal subtotal() {
            return menu.getPrecio().multiply(BigDecimal.valueOf(cantidad));
        }

        boolean mismoPlato(Menu otro, String otrasObservaciones) {
            return menu.getIdMenu() == otro.getIdMenu() && Objects.equals(observaciones, otrasObservaciones);
        }
    }

    private static final int MAXIMO_POR_PLATO = 50;

    private final PedidoDialog vista;
    private final MenuService menuService;
    private final PedidoService pedidoService;
    private final List<Renglon> renglones = new ArrayList<>();

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

    /** Suma el plato a la lista; si ya estaba (con las mismas observaciones) se suma la cantidad. */
    public void agregar() {
        Menu menu = vista.getMenuSeleccionado();
        if (menu == null) {
            vista.mostrarError("Elegí un plato.");
            return;
        }
        String observaciones = vista.getObservaciones().isEmpty() ? null : vista.getObservaciones();
        int cantidad = vista.getCantidad();

        int i = indiceDe(menu, observaciones);
        if (i < 0) {
            renglones.add(new Renglon(menu, cantidad, observaciones));
        } else {
            Renglon antes = renglones.get(i);
            renglones.set(i, new Renglon(menu, Math.min(MAXIMO_POR_PLATO, antes.cantidad() + cantidad), observaciones));
        }
        vista.mostrarRenglones(List.copyOf(renglones));
        vista.listoParaOtro();
    }

    /** Suma o resta uno a un plato de la lista; si llega a cero se saca. */
    public void cambiarCantidad(int indice, int diferencia) {
        Renglon r = renglones.get(indice);
        int cantidad = Math.min(MAXIMO_POR_PLATO, r.cantidad() + diferencia);
        if (cantidad <= 0) {
            renglones.remove(indice);
        } else {
            renglones.set(indice, new Renglon(r.menu(), cantidad, r.observaciones()));
        }
        vista.mostrarRenglones(List.copyOf(renglones));
    }

    public void quitar(int indice) {
        renglones.remove(indice);
        vista.mostrarRenglones(List.copyOf(renglones));
    }

    public boolean hayPendientes() {
        return !renglones.isEmpty();
    }

    /**
     * Guarda los platos de la lista, uno por uno. Si alguno falla (por ejemplo, falta stock),
     * los que ya se guardaron salen de la lista y quedan los que faltan, para corregirlos.
     */
    public void cargar() {
        if (renglones.isEmpty()) {
            vista.mostrarError("Agregá al menos un plato a la lista.");
            return;
        }
        List<Renglon> aCargar = List.copyOf(renglones);
        AtomicInteger guardados = new AtomicInteger();
        vista.bloquear(true);
        Tarea.ejecutar(vista.getBotonCargar(), () -> {
            for (Renglon r : aCargar) {
                pedidoService.guardar(pedidoDe(r));
                guardados.incrementAndGet();
            }
            return aCargar.size();
        }, total -> {
            renglones.clear();
            vista.bloquear(false);
            vista.cerrarCargado();
        }, mensaje -> {
            vista.bloquear(false);
            int ok = guardados.get();
            renglones.subList(0, ok).clear();
            vista.mostrarRenglones(List.copyOf(renglones));
            if (ok > 0) {
                vista.marcarCambios();
                vista.mostrarError("Se cargaron " + ok + " de " + aCargar.size()
                        + " platos. No se pudo cargar " + renglones.get(0).menu().getNombre() + ": " + mensaje);
            } else {
                vista.mostrarError(mensaje);
            }
        });
    }

    private int indiceDe(Menu menu, String observaciones) {
        for (int i = 0; i < renglones.size(); i++) {
            if (renglones.get(i).mismoPlato(menu, observaciones)) {
                return i;
            }
        }
        return -1;
    }

    /** PedidoService.guardar solo usa los ids: el menú y el ticket los busca él en la base. */
    private Pedido pedidoDe(Renglon r) {
        Ticket ticket = new Ticket();
        ticket.setNroTicket(vista.getNroTicket());

        Pedido pedido = new Pedido();
        pedido.setMenu(r.menu());
        pedido.setTicket(ticket);
        pedido.setCantidad(r.cantidad());
        pedido.setObservaciones(r.observaciones());
        return pedido;
    }
}
