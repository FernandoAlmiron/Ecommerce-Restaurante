package org.example.escritorio.vista.admin;

import org.example.Modelo.reserva.Zona;
import org.example.escritorio.controlador.admin.ZonasControlador;
import org.example.escritorio.vista.comun.PanelAbm;

/** Solapa Zonas de Administración (RF-51): las zonas del salón y su capacidad. */
public class ZonasPanel extends PanelAbm<Zona> {

    public ZonasPanel() {
        super(new ZonaTableModel(), 0, new int[]{260, 160, 80}, "Nueva zona");
        ZonasControlador controlador = new ZonasControlador(this);
        conectar(() -> controlador.cargar(null), controlador::nuevo, controlador::modificar,
                controlador::eliminar, controlador::alCambiarSeleccion);
        controlador.cargar(null);
    }
}