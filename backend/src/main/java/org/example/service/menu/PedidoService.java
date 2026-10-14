package org.example.service.menu;

import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.menu.Menu;
import org.example.Modelo.menu.Pedido;
import org.example.repository.facturacion.TicketRepository;
import org.example.repository.menu.MenuRepository;
import org.example.repository.menu.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        pedido.setMenu(menu);
        pedido.setTicket(ticket);
        pedido.setPrecioUnitario(menu.getPrecio());   // el precio lo pone el menu, no el cliente

        Pedido guardado = pedidoRepository.save(pedido);
        menuIngredienteService.descontarStockPorPedido(menu, pedido.getCantidad());
        return guardado;
    }

    public Pedido buscarPorId(int id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Pedido no encontrado"));
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }
}