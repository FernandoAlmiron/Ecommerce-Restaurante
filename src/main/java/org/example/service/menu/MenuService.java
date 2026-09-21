package org.example.service.menu;

import org.example.Modelo.menu.Menu;
import org.example.repository.menu.MenuRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public Menu guardar(Menu menu) {
        if (menu.getNombre() == null || menu.getNombre().isBlank()) {
            throw new IllegalArgumentException("El menu necesita un nombre");
        }
        if (menu.getPrecio() == null || menu.getPrecio().signum() <= 0) {
            throw new IllegalArgumentException("El precio del menu debe ser mayor a 0");
        }
        if (menu.getRestaurante() == null) {
            throw new IllegalArgumentException("El menu necesita un restaurante");
        }
        return menuRepository.save(menu);
    }

    public Menu buscarPorId(int id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Menu no encontrado"));
    }

    public List<Menu> listarTodos() {
        return menuRepository.findAll();
    }
}
