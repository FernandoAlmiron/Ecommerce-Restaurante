package org.example.service.inventario;

import org.example.Modelo.inventario.Stock;
import org.example.repository.inventario.StockRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public Stock guardar(Stock stock) {
        return stockRepository.save(stock);
    }

    public Stock buscarPorId(int idStock) {
        return stockRepository.findById(idStock)
                .orElseThrow(() -> new RuntimeException("Stock no encontrado"));
    }

    public List<Stock> listarTodos() {
        return stockRepository.findAll();
    }

    public Stock aumentarStock(int idStock, double cantidad) {
        Stock stock = buscarPorId(idStock);
        stock.aumentarStock(cantidad);
        return stockRepository.save(stock);
    }

    public Stock disminuirStock(int idStock, double cantidad) {
        Stock stock = buscarPorId(idStock);
        stock.disminuirStock(cantidad);
        return stockRepository.save(stock);
    }

    public boolean hayStockSuficiente(int idStock, double cantidad) {
        Stock stock = buscarPorId(idStock);
        return stock.hayStockSuficiente(cantidad);
    }

    public List<Stock> listarStockBajoMinimo() {
        return stockRepository.findAll().stream()
                .filter(Stock::alcanzoStockMinimo)
                .toList();
    }
    public Stock reponerStock(int idStock, double cantidadRecibida) {
        Stock stock = buscarPorId(idStock);
        stock.setCantidadActual(stock.getCantidadActual() + cantidadRecibida);
        return stockRepository.save(stock);
    }

    public Stock ajustarStock(int idStock, double nuevaCantidad) {
        Stock stock = buscarPorId(idStock);
        stock.setCantidadActual(nuevaCantidad);
        return stockRepository.save(stock);
    }
}