package org.example.escritorio.controlador.admin;

import org.example.Modelo.inventario.Categoria;
import org.example.Modelo.inventario.Proveedores;
import org.example.escritorio.Contexto;
import org.example.escritorio.vista.admin.ProveedorForm;
import org.example.escritorio.vista.admin.ProveedorTableModel;
import org.example.escritorio.vista.admin.ProveedoresPanel;
import org.example.escritorio.vista.comun.FormularioDialog;
import org.example.escritorio.vista.comun.Tarea;
import org.example.service.inventario.CategoriaService;
import org.example.service.inventario.ProveedoresService;
import org.springframework.dao.DataIntegrityViolationException;

import javax.swing.JButton;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Proveedores con ProveedoresService (RF-56). El backend valida que el CUIT tenga 11 números
 * y la base no deja repetir un CUIT.
 */
public class ProveedoresControlador {

    private final ProveedoresPanel vista;
    private final ProveedoresService proveedoresService;
    private final CategoriaService categoriaService;

    public ProveedoresControlador(ProveedoresPanel vista) {
        this.vista = vista;
        this.proveedoresService = Contexto.get(ProveedoresService.class);
        this.categoriaService = Contexto.get(CategoriaService.class);
    }

    /** @param seleccionar número del proveedor a dejar seleccionado, o null para mantener el actual */
    public void cargar(Integer seleccionar) {
        Integer aSeleccionar = seleccionar != null ? seleccionar
                : vista.seleccionado().map(Proveedores::getNroProveedor).orElse(null);

        Tarea.ejecutar(vista.getBotonActualizar(), proveedoresService::listarTodos, lista -> {
            vista.mostrar(lista, lista.size() + " proveedores");
            if (aSeleccionar != null) {
                vista.seleccionar(p -> p.getNroProveedor() == aSeleccionar);
            }
            alCambiarSeleccion();
        });
    }

    public void alCambiarSeleccion() {
        vista.habilitarAcciones(vista.seleccionado().isPresent());
    }

    public void nuevo() {
        abrir(null, vista.getBotonNuevo());
    }

    public void modificar() {
        vista.seleccionado().ifPresent(p -> abrir(p, vista.getBotonModificar()));
    }

    public void eliminar() {
        vista.seleccionado().ifPresent(proveedor -> {
            if (!vista.confirmar("¿Eliminar el proveedor \"" + proveedor.getRazonSocial() + "\"?")) {
                return;
            }
            Tarea.ejecutar(vista.getBotonEliminar(), () -> {
                try {
                    proveedoresService.eliminar(proveedor.getNroProveedor());
                    return true;
                } catch (DataIntegrityViolationException e) {
                    throw new IllegalStateException("El proveedor \"" + proveedor.getRazonSocial()
                            + "\" tiene datos asociados y no se puede eliminar.");
                }
            }, ok -> cargar(null));
        });
    }

    /** Primero trae las categorías (activas, más la que ya tenga el proveedor) y después abre el formulario. */
    private void abrir(Proveedores original, JButton origen) {
        Tarea.ejecutar(origen, () -> categoriaService.listarTodos().stream()
                .filter(c -> c.isActivo() || (original != null && original.getCategoria() != null
                        && c.getNroCategoria() == original.getCategoria().getNroCategoria()))
                .sorted(Comparator.comparing(Categoria::getNombre))
                .toList(), categorias -> {
            ProveedorForm form = new ProveedorForm(original, categorias);
            FormularioDialog.abrir(vista, original == null ? "Nuevo proveedor" : "Modificar proveedor", form,
                    () -> preparar(form, original)
            ).ifPresent(guardado -> cargar(guardado.getNroProveedor()));
        });
    }

    private Callable<Proveedores> preparar(ProveedorForm form, Proveedores original) {
        if (form.getRazonSocial().isEmpty()) {
            throw new IllegalArgumentException("Completá la razón social.");
        }
        if (form.getCategoria() == null) {
            throw new IllegalArgumentException("Elegí la categoría. Si no hay ninguna, cargala en la solapa Categorías.");
        }

        Proveedores datos = new Proveedores();
        datos.setRazonSocial(form.getRazonSocial());
        datos.setCuit(form.getCuit()); // el formato lo valida ProveedoresService
        datos.setTelefono(form.getTelefono().isEmpty() ? null : form.getTelefono());
        datos.setEmail(form.getEmail().isEmpty() ? null : form.getEmail());
        datos.setCategoria(form.getCategoria());

        return () -> {
            try {
                return original == null
                        ? proveedoresService.guardar(datos)
                        : proveedoresService.actualizar(original.getNroProveedor(), datos);
            } catch (DataIntegrityViolationException e) {
                throw new IllegalStateException("Ya hay un proveedor con el CUIT "
                        + ProveedorTableModel.cuit(datos.getCuit()) + ".");
            }
        };
    }
}