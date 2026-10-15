package org.example.escritorio.controlador.cocina;

import org.example.Modelo.inventario.Categoria;
import org.example.Modelo.inventario.Stock;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.cocina.StockDialog;
import org.example.escritorio.vista.comun.Tarea;
import org.example.service.RestauranteService;
import org.example.service.inventario.CategoriaService;
import org.example.service.inventario.StockService;

import java.util.Comparator;

/**
 * Guarda un insumo nuevo o modificado con StockService (RF-30).
 * Al modificar se mantiene la cantidad actual: para cambiarla están Reponer y Ajustar.
 */
public class StockEdicionControlador {

    private final StockDialog vista;
    private final StockService stockService;
    private final CategoriaService categoriaService;
    private final RestauranteService restauranteService;

    public StockEdicionControlador(StockDialog vista) {
        this.vista = vista;
        this.stockService = Contexto.get(StockService.class);
        this.categoriaService = Contexto.get(CategoriaService.class);
        this.restauranteService = Contexto.get(RestauranteService.class);
    }

    /** Las categorías activas, más la del insumo aunque esté inactiva, para no perderla al modificar. */
    public void cargarCategorias() {
        Stock insumo = vista.getInsumo();
        Integer actual = insumo != null && insumo.getCategoria() != null
                ? insumo.getCategoria().getNroCategoria() : null;
        Tarea.ejecutar(vista.getComboCategoria(),
                () -> categoriaService.listarTodos().stream()
                        .filter(c -> c.isActivo() || (actual != null && c.getNroCategoria() == actual))
                        .sorted(Comparator.comparing(Categoria::getNombre))
                        .toList(),
                vista::mostrarCategorias);
    }

    public void guardar() {
        Double cantidad = vista.getCantidad();
        Double minimo = vista.getMinimo();
        if (vista.getNombre().isEmpty() || vista.getUnidad().isEmpty()) {
            vista.mostrarError("Completá el nombre y la unidad del insumo.");
            return;
        }
        if (vista.getCategoria() == null) {
            vista.mostrarError("Elegí la categoría.");
            return;
        }
        if (cantidad == null || minimo == null) {
            vista.mostrarError("Escribí las cantidades con números, por ejemplo 2,5.");
            return;
        }

        Stock datos = new Stock();
        datos.setNombre(vista.getNombre());
        datos.setUnidadMedida(vista.getUnidad());
        datos.setCategoria(vista.getCategoria());
        datos.setStockMinimo(minimo);
        datos.setStockMinimoActivo(vista.isAlertaActiva());

        Stock original = vista.getInsumo();
        int restaurante = Sesion.nroRestaurante();
        Tarea.ejecutar(vista.getBotonGuardar(), () -> {
            if (original == null) {
                datos.setCantidadActual(cantidad);
                datos.setRestaurante(restauranteService.buscarPorId(restaurante));
                return stockService.guardar(datos);
            }
            // StockService.actualizar también pisa la cantidad: se relee justo antes para no deshacer
            // lo que descontaron los pedidos mientras la ventana estaba abierta
            datos.setCantidadActual(stockService.buscarPorId(original.getIdStock()).getCantidadActual());
            return stockService.actualizar(original.getIdStock(), datos);
        }, vista::cerrarCon);
    }
}