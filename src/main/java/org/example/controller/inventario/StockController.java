package org.example.controller.inventario;

import org.example.Modelo.inventario.Stock;
import org.example.service.inventario.StockService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public List<Stock> listarTodos() {
        return stockService.listarTodos();
    }

    @GetMapping("/{id}")
    public Stock buscarPorId(@PathVariable int id) {
        return stockService.buscarPorId(id);
    }

    @GetMapping("/bajo-minimo")
    public List<Stock> listarStockBajoMinimo() {
        return stockService.listarStockBajoMinimo();
    }

    @PostMapping
    public Stock crear(@RequestBody Stock stock) {
        return stockService.guardar(stock);
    }

    @PutMapping("/{id}/reponer")
    public Stock reponerStock(@PathVariable int id, @RequestParam double cantidad) {
        return stockService.reponerStock(id, cantidad);
    }

    @PutMapping("/{id}/ajustar")
    public Stock ajustarStock(@PathVariable int id, @RequestParam double nuevaCantidad) {
        return stockService.ajustarStock(id, nuevaCantidad);
    }
}