package org.example.escritorio.controlador.cocina;

import org.example.Modelo.menu.Menu;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.cocina.MenuDialog;
import org.example.escritorio.vista.cocina.MenuPanel;
import org.example.escritorio.vista.cocina.RecetaDialog;
import org.example.escritorio.vista.comun.Tarea;
import org.example.service.menu.MenuService;
import org.springframework.dao.DataIntegrityViolationException;

import javax.swing.SwingUtilities;

/** Lógica de la carta. Las reglas (nombre obligatorio, precio mayor a 0) están en MenuService. */
public class MenuControlador {

    private final MenuPanel vista;
    private final MenuService menuService;

    public MenuControlador(MenuPanel vista) {
        this.vista = vista;
        this.menuService = Contexto.get(MenuService.class);
    }

    /** Todos los platos del restaurante del empleado, disponibles o no. */
    public void cargar() {
        int restaurante = Sesion.nroRestaurante();
        Tarea.ejecutar(vista.getBotonActualizar(),
                () -> menuService.listarTodos().stream()
                        .filter(m -> m.getRestaurante() != null
                                && m.getRestaurante().getNroRestaurante() == restaurante)
                        .toList(),
                vista::mostrarPlatos);
    }

    public void nuevo() {
        abrirEdicion(null);
    }

    public void modificar() {
        vista.platoSeleccionado().ifPresent(this::abrirEdicion);
    }

    public void receta() {
        vista.platoSeleccionado().ifPresent(plato ->
                new RecetaDialog(SwingUtilities.getWindowAncestor(vista), plato).setVisible(true));
    }

    /** Solo ADMIN ve el botón (RF-27). Si el plato ya tiene pedidos, la base no deja borrarlo. */
    public void eliminar() {
        vista.platoSeleccionado().ifPresent(plato -> {
            if (!vista.confirmar("¿Eliminar el plato \"" + plato.getNombre() + "\" y su receta?")) {
                return;
            }
            Tarea.ejecutar(vista.getBotonEliminar(), () -> {
                try {
                    menuService.eliminar(plato.getIdMenu());
                    return true;
                } catch (DataIntegrityViolationException e) {
                    throw new IllegalStateException("No se puede eliminar \"" + plato.getNombre()
                            + "\" porque ya tiene pedidos. Para sacarlo de la carta, marcalo como no disponible.");
                }
            }, ok -> cargar());
        });
    }

    private void abrirEdicion(Menu plato) {
        MenuDialog dialogo = new MenuDialog(SwingUtilities.getWindowAncestor(vista), plato);
        dialogo.setVisible(true); // es modal: sigue cuando se cierra
        if (dialogo.seGuardo()) {
            cargar();
        }
    }
}