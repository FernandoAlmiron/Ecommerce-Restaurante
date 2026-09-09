package org.example.service.menu;

import org.example.Modelo.inventario.Stock;
import org.example.Modelo.menu.Menu;
import org.example.Modelo.menu.MenuIngrediente;
import org.example.repository.inventario.StockRepository;
import org.example.repository.menu.MenuIngredienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuIngredienteService {

    private final MenuIngredienteRepository menuIngredienteRepository;
    private final StockRepository stockRepository;

    public MenuIngredienteService(MenuIngredienteRepository menuIngredienteRepository, StockRepository stockRepository) {
        this.menuIngredienteRepository = menuIngredienteRepository;
        this.stockRepository = stockRepository;
    }

    public MenuIngrediente guardar(MenuIngrediente menuIngrediente) {
        return menuIngredienteRepository.save(menuIngrediente);
    }

    public MenuIngrediente buscarPorId(int id) {
        return menuIngredienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MenuIngrediente no encontrado"));
    }

    public List<MenuIngrediente> listarPorMenu(Menu menu) {
        return menuIngredienteRepository.findByMenu(menu);
    }

    public List<MenuIngrediente> listarTodos() {
        return menuIngredienteRepository.findAll();
    }

    public MenuIngrediente actualizarCantidad(int id, double nuevaCantidad) {
        MenuIngrediente mi = buscarPorId(id);
        mi.setCantidadNecesaria(nuevaCantidad);
        return menuIngredienteRepository.save(mi);
    }

    public void eliminar(int id) {
        menuIngredienteRepository.deleteById(id);
    }

    public void descontarStockPorPedido(Menu menu, int cantidadPedida) {
        List<MenuIngrediente> ingredientes = menuIngredienteRepository.findByMenu(menu);
        for (MenuIngrediente mi : ingredientes) {
            Stock stock = mi.getStock();
            double aDescontar = mi.calcularCantidadADescontar(cantidadPedida);
            if (stock.getCantidadActual() < aDescontar) {
                throw new IllegalStateException("Stock insuficiente de " + stock.getNombre());
            }
            stock.setCantidadActual(stock.getCantidadActual() - aDescontar);
            stockRepository.save(stock);
        }
    }
}
