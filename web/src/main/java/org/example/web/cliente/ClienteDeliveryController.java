package org.example.web.cliente;

import org.example.Modelo.enums.MetodoPago;
import org.example.Modelo.menu.Menu;
import org.example.Modelo.menu.Pedido;
import org.example.Modelo.persona.Cliente;
import org.example.Modelo.reserva.Envio;
import org.example.dto.EnvioDTO;
import org.example.service.menu.MenuService;
import org.example.service.persona.ClienteService;
import org.example.service.reserva.EnvioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** el delivery cpmleto */

@Controller
public class ClienteDeliveryController {

    private static final String ACTUALIZACION = "fragments/cliente/delivery-pedido :: actualizacion";

    private final MenuService menuService;
    private final EnvioService envioService;
    private final ClienteService clienteService;
    private final Carrito carrito;   // el carrito de ESTA sesión (Spring lo inyecta solo)

    public ClienteDeliveryController(MenuService menuService, EnvioService envioService,
                                     ClienteService clienteService, Carrito carrito) {
        this.menuService = menuService;
        this.envioService = envioService;
        this.clienteService = clienteService;
        this.carrito = carrito;
    }

    // una línea del panel "Tu pedido": el plato, cuántos y cuánto suman
    public record Linea(Menu menu, int cantidad, BigDecimal subtotal) {}

    // ===================== 1. CARTA + CARRITO =====================

    @GetMapping("/delivery")
    public String carta(Model model) {
        model.addAttribute("platos", buscarPlatos(null));
        cargarCarrito(model);
        return "cliente/delivery";
    }

    // buscador "¿Qué se te antoja hoy?" (HTMX, mientras escribís)
    @GetMapping("/delivery/buscar")
    public String buscar(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("platos", buscarPlatos(q));
        model.addAttribute("carrito", carrito);
        return "fragments/cliente/delivery-plato :: platos";
    }

    // + : suma uno. Devuelve la tarjeta del plato y el panel "Tu pedido" actualizados
    @PostMapping("/delivery/{id}/sumar")
    public String sumar(@PathVariable int id, Model model) {
        Menu menu = menuService.buscarPorId(id);
        if (!menu.estaDisponible()) {
            throw new IllegalStateException("Ese plato no está disponible");
        }
        carrito.sumar(id);
        return actualizar(menu, model);
    }

    // − : resta uno (si llega a 0 sale del carrito)
    @PostMapping("/delivery/{id}/restar")
    public String restar(@PathVariable int id, Model model) {
        carrito.restar(id);
        return actualizar(menuService.buscarPorId(id), model);
    }

    // "Agregar una nota a la cocina": se guarda en el carrito al salir del cuadro de texto
    @PostMapping("/delivery/nota")
    @ResponseBody
    public String nota(@RequestParam(required = false) String nota) {
        carrito.setNota(nota);
        return "";
    }

    // ===================== 2. PAGO =====================

    @GetMapping("/delivery/pago")
    public String pago(Model model) {
        if (carrito.isVacio()) {
            return "redirect:/delivery";
        }
        cargarCarrito(model);
        return "cliente/delivery-pago";
    }

    @PostMapping("/delivery/pago")
    public String pagar(@RequestParam String direccionEntrega,
                        @RequestParam(required = false) String nombreReceptor,
                        @RequestParam MetodoPago metodoPago,     // "TARJETA", "QR" o "EFECTIVO"
                        Authentication auth, Model model) {
        if (carrito.isVacio()) {
            return "redirect:/delivery";
        }
        try {
            // el carrito se convierte en lo que espera EnvioService
            EnvioDTO dto = new EnvioDTO();
            dto.setDireccionEntrega(direccionEntrega);
            dto.setNombreReceptor(nombreReceptor);
            dto.setMetodoPago(metodoPago);

            List<EnvioDTO.PedidoDTO> pedidos = new ArrayList<>();
            for (Map.Entry<Integer, Integer> item : carrito.getItems().entrySet()) {
                EnvioDTO.PedidoDTO pedido = new EnvioDTO.PedidoDTO();
                pedido.setIdMenu(item.getKey());
                pedido.setCantidad(item.getValue());
                // la nota para la cocina viaja como observación del primer plato
                if (pedidos.isEmpty() && !carrito.getNota().isBlank()) {
                    pedido.setObservaciones(carrito.getNota());
                }
                pedidos.add(pedido);
            }
            dto.setPedidos(pedidos);

            // crea el ticket, los pedidos (descuenta stock) y el envío PENDIENTE
            Envio envio = envioService.crearEnvioConPedido(dto, auth.getName());
            carrito.vaciar();
            return "redirect:/pedidos/" + envio.getIdEnvio();

        } catch (IllegalArgumentException | IllegalStateException e) {
            // "No hay stock suficiente de ...", "La dirección de entrega es obligatoria", etc.
            model.addAttribute("error", e.getMessage());
            cargarCarrito(model);
            return "cliente/delivery-pago";
        }
    }

    // ===================== 3. MIS PEDIDOS + SEGUIMIENTO =====================

    @GetMapping("/pedidos")
    public String misPedidos(Authentication auth, Model model) {
        Cliente cliente = clienteService.buscarPorUsername(auth.getName());
        model.addAttribute("envios", envioService.listarPorCliente(cliente.getIdCliente()).stream()
                .sorted(Comparator.comparing(Envio::getIdEnvio).reversed())   // los más nuevos primero
                .toList());
        return "cliente/pedidos";
    }

    // confirmación + "Así va tu pedido"
    @GetMapping("/pedidos/{id}")
    public String verPedido(@PathVariable int id, Authentication auth, Model model) {
        cargarPedido(id, auth, model);
        return "cliente/pedido";
    }

    // solo el bloque del seguimiento: HTMX lo vuelve a pedir cada 10 segundos
    @GetMapping("/pedidos/{id}/seguimiento")
    public String seguimiento(@PathVariable int id, Authentication auth, Model model) {
        cargarPedido(id, auth, model);
        return "fragments/cliente/seguimiento :: seguimiento";
    }

    // "Volver a pedir": carga los mismos platos en el carrito y lleva a la carta
    @PostMapping("/pedidos/{id}/repetir")
    public String repetir(@PathVariable int id, Authentication auth) {
        Envio envio = buscarPropio(id, auth);
        for (Pedido pedido : envio.getTicket().getPedidos()) {
            if (pedido.getMenu().estaDisponible()) {
                carrito.agregar(pedido.getMenu().getIdMenu(), pedido.getCantidad());
            }
        }
        return "redirect:/delivery";
    }

    // ===================== auxiliares =====================

    // seguridad: un cliente solo puede ver SUS pedidos, aunque cambie el número en la URL
    private Envio buscarPropio(int id, Authentication auth) {
        Envio envio = envioService.buscarPorId(id);
        if (!auth.getName().equals(envio.getCliente().getUsername())) {
            throw new IllegalStateException("Ese pedido no es tuyo");
        }
        return envio;
    }

    private void cargarPedido(int id, Authentication auth, Model model) {
        Envio envio = buscarPropio(id, auth);
        model.addAttribute("e", envio);
        model.addAttribute("paso", paso(envio));
    }

    // en qué paso de "Así va tu pedido" está (sale del estado REAL del envío)
    //   2 = preparando · 3 = en camino · 5 = entregado (todo hecho) · 0 = cancelado
    private int paso(Envio envio) {
        return switch (envio.getEstado()) {
            case PENDIENTE -> 2;
            case EN_CAMINO -> 3;
            case ENTREGADO -> 5;
            case CANCELADO -> 0;
        };
    }

    // platos disponibles, filtrados por el buscador (sin importar mayúsculas)
    private List<Menu> buscarPlatos(String q) {
        String texto = q == null ? "" : q.trim().toLowerCase();
        return menuService.listarTodos().stream()
                .filter(Menu::estaDisponible)
                .filter(m -> texto.isEmpty()
                        || m.getNombre().toLowerCase().contains(texto)
                        || (m.getDescripcion() != null && m.getDescripcion().toLowerCase().contains(texto)))
                .toList();
    }

    // respuesta de + y −: la tarjeta del plato y el panel, los dos "por afuera" (hx-swap-oob)
    private String actualizar(Menu menu, Model model) {
        model.addAttribute("m", menu);
        model.addAttribute("oob", true);
        cargarCarrito(model);
        return ACTUALIZACION;
    }

    // arma las líneas del panel "Tu pedido" con el total
    private void cargarCarrito(Model model) {
        List<Linea> lineas = carrito.getItems().entrySet().stream()
                .map(item -> {
                    Menu menu = menuService.buscarPorId(item.getKey());
                    BigDecimal subtotal = menu.getPrecio().multiply(BigDecimal.valueOf(item.getValue()));
                    return new Linea(menu, item.getValue(), subtotal);
                })
                .toList();
        model.addAttribute("carrito", carrito);
        model.addAttribute("lineas", lineas);
        model.addAttribute("total", lineas.stream().map(Linea::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
