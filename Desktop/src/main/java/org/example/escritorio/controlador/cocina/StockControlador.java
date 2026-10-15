package org.example.escritorio.controlador.cocina;

import org.example.Modelo.inventario.Stock;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.cocina.StockDialog;
import org.example.escritorio.vista.cocina.StockPanel;
import org.example.escritorio.vista.comun.CantidadDialog;
import org.example.escritorio.vista.comun.Tarea;
import org.example.service.inventario.StockService;
import org.springframework.dao.DataIntegrityViolationException;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.util.concurrent.Callable;

/**
 * Lógica del stock con StockService (RF-30, RF-31).
 * Reponer suma lo recibido; ajustar fija la cantidad contada. El formulario no deja cantidades negativas.
 */
public class StockControlador {

    private final StockPanel vista;
    private final StockService stockService;

    public StockControlador(StockPanel vista) {
        this.vista = vista;
        this.stockService = Contexto.get(StockService.class);
    }

    /** Los insumos del restaurante del empleado. */
    public void cargar() {
        cargarYSeleccionar(null);
    }

    public void nuevo() {
        abrirEdicion(null);
    }

    public void modificar() {
        vista.insumoSeleccionado().ifPresent(this::abrirEdicion);
    }

    public void reponer() {
        vista.insumoSeleccionado().ifPresent(s -> CantidadDialog.pedir(vista, "Reponer " + s.getNombre(),
                        "Cantidad recibida:", s.getUnidadMedida(), 1, 0.001)
                .ifPresent(cantidad -> ejecutar(vista.getBotonReponer(),
                        () -> stockService.reponerStock(s.getIdStock(), cantidad))));
    }

    public void ajustar() {
        vista.insumoSeleccionado().ifPresent(s -> CantidadDialog.pedir(vista, "Ajustar " + s.getNombre(),
                        "Cantidad contada:", s.getUnidadMedida(), s.getCantidadActual(), 0)
                .ifPresent(cantidad -> ejecutar(vista.getBotonAjustar(),
                        () -> stockService.ajustarStock(s.getIdStock(), cantidad))));
    }

    /** Solo ADMIN ve el botón (RF-32). Si el insumo está en alguna receta, la base no deja borrarlo. */
    public void eliminar() {
        vista.insumoSeleccionado().ifPresent(s -> {
            if (!vista.confirmar("¿Eliminar el insumo \"" + s.getNombre() + "\"?")) {
                return;
            }
            Tarea.ejecutar(vista.getBotonEliminar(), () -> {
                try {
                    stockService.eliminar(s.getIdStock());
                    return true;
                } catch (DataIntegrityViolationException e) {
                    throw new IllegalStateException("No se puede eliminar \"" + s.getNombre()
                            + "\" porque está en la receta de algún plato.");
                }
            }, ok -> cargar());
        });
    }

    private void abrirEdicion(Stock insumo) {
        StockDialog dialogo = new StockDialog(SwingUtilities.getWindowAncestor(vista), insumo);
        dialogo.setVisible(true); // es modal: sigue cuando se cierra
        dialogo.getGuardado().ifPresent(guardado -> cargarYSeleccionar(guardado.getIdStock()));
    }

    private void ejecutar(JButton origen, Callable<Stock> accion) {
        Tarea.ejecutar(origen, accion, actualizado -> cargarYSeleccionar(actualizado.getIdStock()));
    }

    private void cargarYSeleccionar(Integer idStock) {
        int restaurante = Sesion.nroRestaurante();
        Tarea.ejecutar(vista.getBotonActualizar(),
                () -> stockService.listarTodos().stream()
                        .filter(s -> s.getRestaurante() != null
                                && s.getRestaurante().getNroRestaurante() == restaurante)
                        .toList(),
                insumos -> {
                    vista.mostrarInsumos(insumos);
                    if (idStock != null) {
                        vista.seleccionarInsumo(idStock);
                    }
                });
    }
}