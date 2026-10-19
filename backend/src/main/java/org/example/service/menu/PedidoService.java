package org.example.service.menu;

import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.menu.Menu;
import org.example.Modelo.menu.Pedido;
import org.example.repository.facturacion.TicketRepository;
import org.example.repository.menu.MenuRepository;
import org.example.repository.menu.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final MenuRepository menuRepository;
    private final TicketRepository ticketRepository;
    private final MenuIngredienteService menuIngredienteService;

    public PedidoService(PedidoRepository pedidoRepository, MenuRepository menuRepository,
                         TicketRepository ticketRepository, MenuIngredienteService menuIngredienteService) {
        this.pedidoRepository = pedidoRepository;
        this.menuRepository = menuRepository;
        this.ticketRepository = ticketRepository;
        this.menuIngredienteService = menuIngredienteService;
    }

    @Transactional
    public Pedido guardar(Pedido pedido) {
        if (pedido.getMenu() == null || pedido.getTicket() == null) {
            throw new IllegalArgumentException("El pedido necesita un menu y un ticket");
        }
        if (pedido.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        // se buscan los objetos reales en la base (el JSON solo trae los ids)
        Menu menu = menuRepository.findById(pedido.getMenu().getIdMenu())
                .orElseThrow(() -> new IllegalArgumentException("Menu no encontrado"));
        Ticket ticket = ticketRepository.findById(pedido.getTicket().getNroTicket())
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));

        if (!menu.estaDisponible()) {
            throw new IllegalStateException("El menu '" + menu.getNombre() + "' no esta disponible");
        }
        if (ticket.getFacturacion() != null) {
            throw new IllegalStateException("El ticket ya fue facturado: no se le pueden agregar pedidos");
        }

        pedido.setIdPedido(0);
        pedido.setListo(false);                       // todo pedido nuevo entra pendiente para la cocina
        pedido.setMenu(menu);
        pedido.setTicket(ticket);
        pedido.setPrecioUnitario(menu.getPrecio());   // el precio lo pone el menu, no el cliente

        Pedido guardado = pedidoRepository.save(pedido);
        menuIngredienteService.descontarStockPorPedido(menu, pedido.getCantidad());
        return guardado;
    }

    /**
     * La cocina termino algunos platos de la comanda: marca como listos esos pedidos del ticket.
     * Los que no se pasan siguen pendientes y la comanda sigue apareciendo con ellos.
     * Con idsPedido null o vacio se marcan todos los pendientes (la comanda entera).
     * Si despues se cargan mas pedidos al mismo ticket, esos vuelven a aparecer en la comanda.
     * Devuelve cuantos pedidos se marcaron.
     */
    @Transactional
    public int marcarListos(int nroTicket, Collection<Integer> idsPedido) {
        Ticket ticket = ticketRepository.findById(nroTicket)
                .orElseThrow(() -> new java.util.NoSuchElementException("Ticket no encontrado"));
        List<Pedido> pendientes = ticket.getPedidos() == null ? List.of()
                : ticket.getPedidos().stream().filter(p -> !p.isListo()).toList();

        List<Pedido> aMarcar = pendientes;
        if (idsPedido != null && !idsPedido.isEmpty()) {
            Set<Integer> pendientesIds = pendientes.stream().map(Pedido::getIdPedido).collect(Collectors.toSet());
            if (!pendientesIds.containsAll(idsPedido)) {
                throw new IllegalStateException("Hay pedidos que no son de este ticket o que ya estaban listos");
            }
            aMarcar = pendientes.stream().filter(p -> idsPedido.contains(p.getIdPedido())).toList();
        }
        if (aMarcar.isEmpty()) {
            throw new IllegalStateException("La comanda no tiene platos pendientes");
        }
        aMarcar.forEach(p -> p.setListo(true));
        pedidoRepository.saveAll(aMarcar);
        return aMarcar.size();
    }

    public Pedido buscarPorId(int id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Pedido no encontrado"));
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }
}