package org.example.web.cliente;

import org.example.Modelo.menu.Menu;
import org.example.service.menu.MenuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ClienteInicioController {

    private final MenuService menuService;

    public ClienteInicioController(MenuService menuService) {
        this.menuService = menuService;
    }

    // landing pública: la carta + "Iniciar / Registrarme"
    @GetMapping("/")
    public String landing(Model model) {
        model.addAttribute("platos", platosDisponibles());
        return "cliente/inicio/landing";
    }

    // después del login se llega acá (ver SeguridadWebConfig):
    // la carta con la barra de abajo
    @GetMapping("/inicio")
    public String inicio(Model model) {
        model.addAttribute("platos", platosDisponibles());
        return "cliente/inicio/inicio";
    }

    private List<Menu> platosDisponibles() {
        return menuService.listarTodos().stream()
                .filter(Menu::estaDisponible)
                .toList();
    }
}