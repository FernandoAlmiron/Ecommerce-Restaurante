package org.example.controller.menu;

import org.example.Modelo.menu.MenuIngrediente;
import org.example.service.menu.MenuIngredienteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu-ingredientes")
public class MenuIngredienteController {

    private final MenuIngredienteService menuIngredienteService;

    public MenuIngredienteController(MenuIngredienteService menuIngredienteService) {
        this.menuIngredienteService = menuIngredienteService;
    }

    @GetMapping
    public List<MenuIngrediente> listarTodos() {
        return menuIngredienteService.listarTodos();
    }

    @GetMapping("/{id}")
    public MenuIngrediente buscarPorId(@PathVariable int id) {
        return menuIngredienteService.buscarPorId(id);
    }

    @PostMapping
    public MenuIngrediente crear(@RequestBody MenuIngrediente menuIngrediente) {
        menuIngrediente.setIdMenuIngrediente(0);
        return menuIngredienteService.guardar(menuIngrediente);
    }

    @PutMapping("/{id}")
    public MenuIngrediente actualizarCantidad(@PathVariable int id, @RequestParam double cantidad) {
        return menuIngredienteService.actualizarCantidad(id, cantidad);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable int id) {
        menuIngredienteService.eliminar(id);
    }
}
