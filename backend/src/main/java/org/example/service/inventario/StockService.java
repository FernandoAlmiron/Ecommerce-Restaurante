package org.example.service.inventario;

import org.example.Modelo.inventario.Stock;
import org.example.repository.inventario.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new java.util.NoSuchElementException("Stock no encontrado"));
    }

    public List<Stock> listarTodos() {
        return stockRepository.findAll();
    }

    // Los movimientos de cantidad bloquean la fila (findByIdForUpdate) dentro de la transacción,
    // igual que el descuento por pedido: si los dos pasan a la vez, ninguno pisa al otro.

    @Transactional
    public Stock aumentarStock(int idStock, double cantidad) {
        validarPositiva(cantidad);
        Stock stock = bloquear(idStock);
        stock.aumentarStock(cantidad);
        return stockRepository.save(stock);
    }

    @Transactional
    public Stock disminuirStock(int idStock, double cantidad) {
        validarPositiva(cantidad);
        Stock stock = bloquear(idStock);
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
    /** RF-30: lo recibido de un proveedor se suma; tiene que ser mayor a 0. */
    @Transactional
    public Stock reponerStock(int idStock, double cantidadRecibida) {
        validarPositiva(cantidadRecibida);
        Stock stock = bloquear(idStock);
        stock.setCantidadActual(stock.getCantidadActual() + cantidadRecibida);
        return stockRepository.save(stock);
    }

    /** RF-30: corrige la cantidad después de contar el depósito; puede ser 0, no negativa. */
    @Transactional
    public Stock ajustarStock(int idStock, double nuevaCantidad) {
        if (!(nuevaCantidad >= 0)) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        Stock stock = bloquear(idStock);
        stock.setCantidadActual(nuevaCantidad);
        return stockRepository.save(stock);
    }

    /**
     * Cambia los datos del insumo, NO la cantidad actual: esa solo la mueven reponer, ajustar y los pedidos.
     * Así editar el nombre no deshace un descuento hecho mientras la ventana estaba abierta.
     */
    public Stock actualizar(int idStock, Stock datos) {
        Stock actual = buscarPorId(idStock);
        actual.setNombre(datos.getNombre());
        actual.setUnidadMedida(datos.getUnidadMedida());
        actual.setStockMinimo(datos.getStockMinimo());
        actual.setStockMinimoActivo(datos.isStockMinimoActivo());
        actual.setCategoria(datos.getCategoria());
        return stockRepository.save(actual);
    }

    private Stock bloquear(int idStock) {
        return stockRepository.findByIdForUpdate(idStock)
                .orElseThrow(() -> new java.util.NoSuchElementException("Stock no encontrado"));
    }

    private static void validarPositiva(double cantidad) {
        if (!(cantidad > 0)) {   // también rechaza NaN
            throw new IllegalArgumentException("La cantidad tiene que ser mayor a 0");
        }
    }

    public void eliminar(int idStock) {
        buscarPorId(idStock);   // 404 si no existe
        stockRepository.deleteById(idStock);   // si tiene datos asociados la base lo rechaza (409)
    }
}