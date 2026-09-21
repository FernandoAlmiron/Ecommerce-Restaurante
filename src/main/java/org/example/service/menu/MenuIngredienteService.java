package org.example.service.menu;

import org.example.Modelo.inventario.Stock;
import org.example.Modelo.menu.Menu;
import org.example.Modelo.menu.MenuIngrediente;
import org.example.repository.inventario.StockRepository;
import org.example.repository.menu.MenuIngredienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
                .orElseThrow(() -> new java.util.NoSuchElementException("MenuIngrediente no encontrado"));
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

    @Transactional
    public void descontarStockPorPedido(Menu menu, int cantidadPedida) {
        Map<Integer, Double> requerido = cantidadesRequeridas(menu, cantidadPedida);

        // 1) bloquear y verificar TODO el stock antes de descontar nada
        Map<Integer, Stock> bloqueados = new TreeMap<>();
        for (Map.Entry<Integer, Double> e : requerido.entrySet()) {
            Stock stock = stockRepository.findByIdForUpdate(e.getKey())
                    .orElseThrow(() -> new IllegalStateException("Stock no encontrado: " + e.getKey()));
            if (!stock.hayStockSuficiente(e.getValue())) {
                throw new IllegalStateException("Stock insuficiente de " + stock.getNombre());
            }
            bloqueados.put(e.getKey(), stock);
        }

        // 2) descontar
        for (Map.Entry<Integer, Stock> e : bloqueados.entrySet()) {
            Stock stock = e.getValue();
            stock.setCantidadActual(stock.getCantidadActual() - requerido.get(e.getKey()));
            stockRepository.save(stock);
        }
    }
    private Map<Integer, Double> cantidadesRequeridas(Menu menu, int cantidad) {
        Map<Integer, Double> requerido = new TreeMap<>();
        for (MenuIngrediente mi : menuIngredienteRepository.findByMenu(menu)) {
            requerido.merge(mi.getStock().getIdStock(), mi.calcularCantidadADescontar(cantidad), Double::sum);
        }
        return requerido;
    }
}