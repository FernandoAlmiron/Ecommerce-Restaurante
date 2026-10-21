package org.example.escritorio.vista.admin;

import org.example.Modelo.inventario.Categoria;
import org.example.escritorio.controlador.admin.CategoriasControlador;
import org.example.escritorio.vista.comun.PanelAbm;

/** Solapa Categorías de Administración (RF-56): las categorías que usan el stock y los proveedores. */
public class CategoriasPanel extends PanelAbm<Categoria> {

    public CategoriasPanel() {
        super(new CategoriaTableModel(), 0, new int[]{200, 360, 80}, "Nueva categoría");
        CategoriasControlador controlador = new CategoriasControlador(this);
        conectar(() -> controlador.cargar(null), controlador::nuevo, controlador::modificar,
                controlador::eliminar, controlador::alCambiarSeleccion);
        controlador.cargar(null);
    }
}