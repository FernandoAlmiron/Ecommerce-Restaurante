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
        return menuRepository.save(menu);
    }

    public Menu buscarPorId(int id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu no encontrado"));
    }

    public List<Menu> listarTodos() {
        return menuRepository.findAll();
    }
}
