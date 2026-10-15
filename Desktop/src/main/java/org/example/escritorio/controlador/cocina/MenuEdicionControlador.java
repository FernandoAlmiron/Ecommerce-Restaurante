package org.example.escritorio.controlador.cocina;

import org.example.Modelo.menu.Menu;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.cocina.MenuDialog;
import org.example.escritorio.vista.comun.Tarea;
import org.example.service.RestauranteService;
import org.example.service.menu.MenuService;

import java.math.BigDecimal;

/**
 * Guarda un plato nuevo o modificado con MenuService (RF-26).
 * Al modificar, MenuService mantiene el restaurante del plato; al crear, se usa el del empleado.
 */
public class MenuEdicionControlador {

    private final MenuDialog vista;
    private final MenuService menuService;
    private final RestauranteService restauranteService;

    public MenuEdicionControlador(MenuDialog vista) {
        this.vista = vista;
        this.menuService = Contexto.get(MenuService.class);
        this.restauranteService = Contexto.get(RestauranteService.class);
    }

    public void guardar() {
        BigDecimal precio = vista.getPrecio();
        if (precio == null) {
            vista.mostrarError("Escribí el precio con números, por ejemplo 12500,00.");
            return;
        }

        Menu datos = new Menu();
        datos.setNombre(vista.getNombre());
        datos.setDescripcion(vista.getDescripcion().isEmpty() ? null : vista.getDescripcion());
        datos.setPrecio(precio);
        datos.setDisponible(vista.isDisponible());

        Menu original = vista.getPlato();
        int restaurante = Sesion.nroRestaurante();
        Tarea.ejecutar(vista.getBotonGuardar(), () -> {
            if (original == null) {
                datos.setRestaurante(restauranteService.buscarPorId(restaurante));
                return menuService.guardar(datos);
            }
            return menuService.actualizar(original.getIdMenu(), datos);
        }, guardado -> vista.cerrarGuardado());
    }
}