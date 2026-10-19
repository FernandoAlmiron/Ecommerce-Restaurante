package org.example.escritorio.controlador.cocina;

import org.example.Modelo.inventario.Stock;
import org.example.Modelo.menu.MenuIngrediente;
import org.example.escritorio.Contexto;
import org.example.escritorio.vista.cocina.RecetaDialog;
import org.example.escritorio.vista.comun.CantidadDialog;
import org.example.escritorio.vista.comun.Tarea;
import org.example.service.inventario.StockService;
import org.example.service.menu.MenuIngredienteService;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lógica de la receta (RF-28) con MenuIngredienteService.
 * Un insumo va una sola vez por plato: el combo solo ofrece los que todavía no están en la receta.
 */
public class RecetaControlador {

    private final RecetaDialog vista;
    private final MenuIngredienteService menuIngredienteService;
    private final StockService stockService;

    public RecetaControlador(RecetaDialog vista) {
        this.vista = vista;
        this.menuIngredienteService = Contexto.get(MenuIngredienteService.class);
        this.stockService = Contexto.get(StockService.class);
    }

    private record Datos(List<MenuIngrediente> receta, List<Stock> paraAgregar) {
    }

    public void cargar() {
        int restaurante = vista.getPlato().getRestaurante().getNroRestaurante();
        Tarea.ejecutar(vista.getTabla(), () -> {
            List<MenuIngrediente> receta = menuIngredienteService.listarPorMenu(vista.getPlato()).stream()
                    .sorted(Comparator.comparing(mi -> mi.getStock().getNombre()))
                    .toList();
            Set<Integer> usados = receta.stream()
                    .map(mi -> mi.getStock().getIdStock())
                    .collect(Collectors.toSet());
            List<Stock> paraAgregar = stockService.listarTodos().stream()
                    .filter(s -> s.getRestaurante() != null
                            && s.getRestaurante().getNroRestaurante() == restaurante)
                    .filter(s -> !usados.contains(s.getIdStock()))
                    .sorted(Comparator.comparing(Stock::getNombre))
                    .toList();
            return new Datos(receta, paraAgregar);
        }, datos -> vista.mostrarReceta(datos.receta(), datos.paraAgregar()));
    }

    public void agregar() {
        Stock insumo = vista.getInsumoElegido();
        Double cantidad = vista.getCantidad();
        if (insumo == null) {
            vista.mostrarError("Elegí el insumo.");
            return;
        }
        if (cantidad == null || cantidad <= 0) {
            vista.mostrarError("La cantidad tiene que ser mayor a 0.");
            return;
        }

        MenuIngrediente nuevo = new MenuIngrediente();
        nuevo.setMenu(vista.getPlato());
        nuevo.setStock(insumo);
        nuevo.setCantidadNecesaria(cantidad);

        Tarea.ejecutar(vista.getBotonAgregar(), () -> menuIngredienteService.guardar(nuevo), guardado -> cargar());
    }

    public void cambiarCantidad() {
        vista.ingredienteSeleccionado().ifPresent(mi -> CantidadDialog.pedir(vista, "Cambiar cantidad",
                        mi.getStock().getNombre() + " por porción:", mi.getStock().getUnidadMedida(),
                        mi.getCantidadNecesaria(), 0.001)
                .ifPresent(cantidad -> Tarea.ejecutar(vista.getBotonCambiarCantidad(),
                        () -> menuIngredienteService.actualizarCantidad(mi.getIdMenuIngrediente(), cantidad),
                        actualizado -> cargar())));
    }

    public void quitar() {
        vista.ingredienteSeleccionado().ifPresent(mi -> {
            if (vista.confirmar("¿Quitar " + mi.getStock().getNombre() + " de la receta?")) {
                Tarea.ejecutar(vista.getBotonQuitar(), () -> {
                    menuIngredienteService.eliminar(mi.getIdMenuIngrediente());
                    return true;
                }, ok -> cargar());
            }
        });
    }
}