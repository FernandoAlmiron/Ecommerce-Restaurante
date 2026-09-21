package org.example.controller.menu;

import org.example.Modelo.menu.Menu;
import org.example.service.menu.MenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<Menu> listarTodos() {
        return menuService.listarTodos();
    }

    @GetMapping("/{id}")
    public Menu buscarPorId(@PathVariable int id) {
        return menuService.buscarPorId(id);
    }

    @PostMapping
    public Menu crear(@RequestBody Menu menu) {
        menu.setIdMenu(0);
        return menuService.guardar(menu);
    }
}
