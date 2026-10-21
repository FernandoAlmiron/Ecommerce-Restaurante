package org.example.escritorio.vista.admin;

import org.example.Modelo.inventario.Proveedores;
import org.example.escritorio.controlador.admin.ProveedoresControlador;
import org.example.escritorio.vista.comun.PanelAbm;

/** Solapa Proveedores de Administración (RF-56). */
public class ProveedoresPanel extends PanelAbm<Proveedores> {

    public ProveedoresPanel() {
        super(new ProveedorTableModel(), 0, new int[]{240, 120, 120, 220, 140}, "Nuevo proveedor");
        ProveedoresControlador controlador = new ProveedoresControlador(this);
        conectar(() -> controlador.cargar(null), controlador::nuevo, controlador::modificar,
                controlador::eliminar, controlador::alCambiarSeleccion);
        controlador.cargar(null);
    }
}