package org.example.web;

import org.example.Modelo.enums.EstadoReserva;
import org.example.Modelo.enums.MetodoPago;
import org.example.Modelo.enums.OrigenTicket;
import org.example.Modelo.facturacion.Facturacion;
import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.menu.Menu;
import org.example.Modelo.menu.Pedido;
import org.example.Modelo.reserva.Reserva;
import org.example.service.RestauranteService;
import org.example.service.facturacion.FacturacionService;
import org.example.service.facturacion.TicketService;
import org.example.service.menu.MenuService;
import org.example.service.menu.PedidoService;
import org.example.service.reserva.ReservaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/salon")
public class AdminSalonController {

    private static final String DETALLE = "fragments/admin/ticket-detalle :: detalle";

    private final TicketService ticketService;
    private final PedidoService pedidoService;
    private final MenuService menuService;
    private final ReservaService reservaService;
    private final RestauranteService restauranteService;
    private final FacturacionService facturacionService;

    public AdminSalonController(TicketService ticketService, PedidoService pedidoService,
                                MenuService menuService, ReservaService reservaService,
                                RestauranteService restauranteService, FacturacionService facturacionService) {
        this.ticketService = ticketService;
        this.pedidoService = pedidoService;
        this.menuService = menuService;
        this.reservaService = reservaService;
        this.restauranteService = restauranteService;
        this.facturacionService = facturacionService;
    }

    // página: tickets del salón + datos para abrir una mesa
    @GetMapping
    public String verSalon(Model model) {
        model.addAttribute("tickets", ticketService.listarTodos().stream()
                .filter(t -> t.getOrigen() == OrigenTicket.SALON)
                .toList());
        model.addAttribute("restaurantes", restauranteService.listarTodos());
        model.addAttribute("reservas", reservaService.listarTodos().stream()
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA)
                .toList());
        return "admin/salon";
    }

    // abrir una mesa (ticket nuevo, vacío)
    @PostMapping
    public String abrirMesa(@RequestParam int nroRestaurante,
                            @RequestParam(required = false) Integer nroReserva,
                            Model model) {
        Ticket datos = new Ticket();
        datos.setRestaurante(restauranteService.buscarPorId(nroRestaurante));

        if (nroReserva != null) {
            Reserva reserva = new Reserva();
            reserva.setNroReserva(nroReserva);
            datos.setReserva(reserva);
        }

        model.addAttribute("t", ticketService.crear(datos));
        return "fragments/admin/ticket-fila :: fila";
    }

    // ver el detalle de un ticket
    @GetMapping("/{id}")
    public String verDetalle(@PathVariable int id, Model model) {
        cargarDetalle(id, model);
        return DETALLE;
    }

    // ===== ETAPA 1: ABIERTO =====

    // agregar un pedido
    @PostMapping("/{id}/pedidos")
    public String agregarPedido(@PathVariable int id,
                                @RequestParam int idMenu,
                                @RequestParam int cantidad,
                                @RequestParam(required = false) String observaciones,
                                Model model) {
        Pedido pedido = new Pedido();
        pedido.setMenu(menuService.buscarPorId(idMenu));
        pedido.setTicket(ticketService.buscarPorId(id));
        pedido.setCantidad(cantidad);
        pedido.setObservaciones(observaciones);

        pedidoService.guardar(pedido);   // valida, pone el precio y descuenta stock

        cargarDetalle(id, model);
        return DETALLE;
    }

    // cerrar la cuenta: se crea la facturación (queda pendiente de pago)
    @PostMapping("/{id}/cerrar")
    public String cerrarCuenta(@PathVariable int id, @RequestParam MetodoPago metodoPago, Model model) {
        Ticket ticket = ticketService.buscarPorId(id);

        Facturacion datos = new Facturacion();
        datos.setTicket(ticket);
        datos.setMetodoPago(metodoPago);

        Facturacion factura = facturacionService.crear(datos);   // calcula el total desde los pedidos
        ticket.setFacturacion(factura);   // avisarle al ticket que ahora tiene facturación (ver doc)

        cargarDetalle(id, model);
        return DETALLE;
    }

    // ===== ETAPA 2: POR COBRAR =====

    // aplicar propina (5, 10 o 15 %)
    @PutMapping("/{id}/propina")
    public String aplicarPropina(@PathVariable int id, @RequestParam int porcentaje, Model model) {
        facturacionService.aplicarPropina(nroFacturacion(id), porcentaje);
        cargarDetalle(id, model);
        return DETALLE;
    }

    // confirmar pago → PAGADO
    @PutMapping("/{id}/pagar")
    public String confirmarPago(@PathVariable int id, Model model) {
        facturacionService.confirmarPago(nroFacturacion(id));
        cargarDetalle(id, model);
        return DETALLE;
    }

    // ===== auxiliares =====

    // busca la facturación del ticket (si todavía no cerró la cuenta, avisa)
    private int nroFacturacion(int idTicket) {
        Ticket ticket = ticketService.buscarPorId(idTicket);
        if (ticket.getFacturacion() == null) {
            throw new IllegalStateException("La cuenta todavía no está cerrada");
        }
        return ticket.getFacturacion().getNroFacturacion();
    }

    // lo que necesita el panel de detalle
    private void cargarDetalle(int id, Model model) {
        model.addAttribute("t", ticketService.buscarPorId(id));
        model.addAttribute("platos", menuService.listarTodos().stream()
                .filter(Menu::estaDisponible)
                .toList());
        model.addAttribute("oob", true);   // actualizar también la fila de la izquierda
    }
}