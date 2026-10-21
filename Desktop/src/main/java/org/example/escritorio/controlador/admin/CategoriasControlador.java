package org.example.escritorio.controlador.admin;

import org.example.Modelo.inventario.Categoria;
import org.example.escritorio.Contexto;
import org.example.escritorio.vista.admin.CategoriaForm;
import org.example.escritorio.vista.admin.CategoriasPanel;
import org.example.escritorio.vista.comun.FormularioDialog;
import org.example.escritorio.vista.comun.Tarea;
import org.example.service.inventario.CategoriaService;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.concurrent.Callable;

/** Categorías de insumos y proveedores con CategoriaService (RF-56). Son comunes a todo el sistema. */
public class CategoriasControlador {

    private final CategoriasPanel vista;
    private final CategoriaService categoriaService;
    private List<Categoria> categorias = List.of(); // lo último que se cargó, para avisar nombres repetidos

    public CategoriasControlador(CategoriasPanel vista) {
        this.vista = vista;
        this.categoriaService = Contexto.get(CategoriaService.class);
    }

    /** @param seleccionar número de la categoría a dejar seleccionada, o null para mantener la actual */
    public void cargar(Integer seleccionar) {
        Integer aSeleccionar = seleccionar != null ? seleccionar
                : vista.seleccionado().map(Categoria::getNroCategoria).orElse(null);

        Tarea.ejecutar(vista.getBotonActualizar(), categoriaService::listarTodos, lista -> {
            categorias = lista;
            long activas = lista.stream().filter(Categoria::isActivo).count();
            vista.mostrar(lista, lista.size() + " categorías · " + activas + " activas");
            if (aSeleccionar != null) {
                vista.seleccionar(c -> c.getNroCategoria() == aSeleccionar);
            }
            alCambiarSeleccion();
        });
    }

    public void alCambiarSeleccion() {
        vista.habilitarAcciones(vista.seleccionado().isPresent());
    }

    public void nuevo() {
        abrir(null);
    }

    public void modificar() {
        vista.seleccionado().ifPresent(this::abrir);
    }

    public void eliminar() {
        vista.seleccionado().ifPresent(categoria -> {
            if (!vista.confirmar("¿Eliminar la categoría \"" + categoria.getNombre() + "\"?")) {
                return;
            }
            Tarea.ejecutar(vista.getBotonEliminar(), () -> {
                try {
                    categoriaService.eliminar(categoria.getNroCategoria());
                    return true;
                } catch (DataIntegrityViolationException e) {
                    throw new IllegalStateException("La categoría \"" + categoria.getNombre() + "\" tiene insumos "
                            + "o proveedores cargados y no se puede eliminar. Desactivala para que no se use más.");
                }
            }, ok -> cargar(null));
        });
    }

    private void abrir(Categoria original) {
        CategoriaForm form = new CategoriaForm(original);
        FormularioDialog.abrir(vista, original == null ? "Nueva categoría" : "Modificar categoría", form,
                () -> preparar(form, original)
        ).ifPresent(guardada -> cargar(guardada.getNroCategoria()));
    }

    private Callable<Categoria> preparar(CategoriaForm form, Categoria original) {
        String nombre = form.getNombre();
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("Completá el nombre de la categoría.");
        }
        boolean repetido = categorias.stream().anyMatch(c -> c.getNombre() != null
                && c.getNombre().equalsIgnoreCase(nombre)
                && (original == null || c.getNroCategoria() != original.getNroCategoria()));
        if (repetido) {
            throw new IllegalArgumentException("Ya hay una categoría llamada \"" + nombre + "\".");
        }

        Categoria datos = new Categoria();
        datos.setNombre(nombre);
        datos.setDescripcion(form.getDescripcion().isEmpty() ? null : form.getDescripcion());
        datos.setActivo(form.isActiva());

        return () -> original == null
                ? categoriaService.guardar(datos)
                : categoriaService.actualizar(original.getNroCategoria(), datos);
    }
}