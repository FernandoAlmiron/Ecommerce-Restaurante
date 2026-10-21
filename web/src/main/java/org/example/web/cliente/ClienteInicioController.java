package org.example.web.cliente;

import org.example.Modelo.menu.Menu;
import org.example.service.menu.MenuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClienteInicioController {

    private final MenuService menuService;

    public ClienteInicioController(MenuService menuService) {
        this.menuService = menuService;
    }

    // landing pública: la carta + "Iniciar / Registrarme"
    @GetMapping("/")
    public String landing(Model model) {
        model.addAttribute("platos", menuService.listarTodos().stream()
                .filter(Menu::estaDisponible)
                .toList());
        return "cliente/landing";
    }

    // después del login: ¿presencial o delivery?
    @GetMapping("/elegir")
    public String elegir() {
        return "cliente/elegir";
    }
}