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
        validar(menu);
        return menuRepository.save(menu);
    }

    public Menu actualizar(int idMenu, Menu datos) {
        Menu actual = buscarPorId(idMenu);   // 404 si no existe
        datos.setRestaurante(actual.getRestaurante());   // el restaurante del plato no cambia
        validar(datos);                                  // se valida ANTES de tocar el registro
        actual.setNombre(datos.getNombre());
        actual.setDescripcion(datos.getDescripcion());
        actual.setPrecio(datos.getPrecio());
        actual.setDisponible(datos.isDisponible());
        return menuRepository.save(actual);
    }

    public void eliminar(int idMenu) {
        buscarPorId(idMenu);   // 404 si no existe
        menuRepository.deleteById(idMenu);   // si tiene pedidos la base lo rechaza (409) y no se pierde la receta
    }

    private void validar(Menu menu) {
        if (menu.getNombre() == null || menu.getNombre().isBlank()) {
            throw new IllegalArgumentException("El menu necesita un nombre");
        }
        if (menu.getPrecio() == null || menu.getPrecio().signum() <= 0) {
            throw new IllegalArgumentException("El precio del menu debe ser mayor a 0");
        }
        if (menu.getRestaurante() == null) {
            throw new IllegalArgumentException("El menu necesita un restaurante");
        }
    }

    public Menu buscarPorId(int id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Menu no encontrado"));
    }

    public List<Menu> listarTodos() {
        return menuRepository.findAll();
    }
}