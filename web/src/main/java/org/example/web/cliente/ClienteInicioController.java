package org.example.web.cliente;

import org.example.Modelo.menu.Menu;
import org.example.Modelo.persona.Cliente;
import org.example.service.menu.MenuService;
import org.example.service.persona.ClienteService;
import org.example.service.reserva.ReservaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class ClienteInicioController {

    private final MenuService menuService;
    private final ReservaService reservaService;
    private final ClienteService clienteService;

    public ClienteInicioController(MenuService menuService, ReservaService reservaService,
                                   ClienteService clienteService) {
        this.menuService = menuService;
        this.reservaService = reservaService;
        this.clienteService = clienteService;
    }

    // landing pública: la carta + "Iniciar / Registrarme"
    @GetMapping("/")
    public String landing(Model model) {
        model.addAttribute("platos", platosDisponibles());
        return "cliente/landing";
    }

    // después del login: si ya tiene reservas va directo a la carta,
    // si es nuevo elige presencial o delivery

    @GetMapping("/elegir")
    public String elegir(Principal principal) {
        if (tieneReservas(principal.getName())) {
            return "redirect:/inicio";
        }
        return "cliente/elegir";
    }

    // la carta con la barra de abajo (para usuarios logueados)
    @GetMapping("/inicio")
    public String inicio(Model model) {
        model.addAttribute("platos", platosDisponibles());
        return "cliente/inicio";
    }

    private List<Menu> platosDisponibles() {
        return menuService.listarTodos().stream()
                .filter(Menu::estaDisponible)
                .toList();
    }

    // un usuario recién registrado puede no tener Cliente todavía: en ese caso cuenta como nuevo
    private boolean tieneReservas(String username) {
        Cliente cliente = clienteService.buscarPorUsername(username);
        if (cliente == null) {
            return false;
        }
        return !reservaService.listarPorCliente(cliente.getIdCliente()).isEmpty();
    }
}