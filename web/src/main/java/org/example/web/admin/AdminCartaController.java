package org.example.web.admin;
import org.example.Modelo.menu.Menu;
import org.example.service.RestauranteService;
import org.example.service.menu.MenuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/carta")
public class AdminCartaController {

    private final MenuService menuService;
    private final RestauranteService restauranteService;

    public AdminCartaController(MenuService menuService, RestauranteService restauranteService) {
        this.menuService = menuService;
        this.restauranteService = restauranteService;
    }

    // ver todos los platos (+ restaurantes para el desplegable del formulario)
    @GetMapping
    public String verCarta(Model model) {
        model.addAttribute("platos", menuService.listarTodos());
        model.addAttribute("restaurantes", restauranteService.listarTodos());
        return "admin/carta";
    }

    // crear un plato
    @PostMapping
    public String crear(@ModelAttribute Menu plato, @RequestParam int nroRestaurante, Model model) {
        plato.setRestaurante(restauranteService.buscarPorId(nroRestaurante));
        model.addAttribute("p", menuService.guardar(plato));
        return "fragments/admin/plato-fila :: fila";
    }

    // eliminar un plato (borrado lógico)
    @DeleteMapping("/{id}")
    @ResponseBody
    public String eliminar(@PathVariable int id) {
        menuService.eliminar(id);
        return "";
    }

    // cambiar disponibilidad (disponible <-> no disponible)
    @PutMapping("/{id}/disponibilidad")
    public String cambiarDisponibilidad(@PathVariable int id, Model model) {
        Menu plato = menuService.buscarPorId(id);

        if (plato.estaDisponible()) {
            plato.marcarNoDisponible();
        } else {
            plato.marcarDisponible();
        }

        model.addAttribute("p", menuService.guardar(plato));
        return "fragments/admin/plato-fila :: fila";
    }
}