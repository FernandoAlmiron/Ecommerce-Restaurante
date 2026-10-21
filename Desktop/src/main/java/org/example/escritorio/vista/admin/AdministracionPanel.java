package org.example.escritorio.vista.admin;

import org.example.escritorio.vista.comun.PantallaConSolapas;

/**
 * Área de Administración (solo ADMIN): la configuración del restaurante.
 * Cada solapa es una pantalla con su propio controlador.
 * Restaurante (RF-49), Zonas (RF-51), Sectores (RF-52), Categorías y Proveedores (RF-56).
 */
public class AdministracionPanel extends PantallaConSolapas {

    public AdministracionPanel() {
        super("Administración");
        agregar("Restaurante", new RestaurantePanel());
        agregar("Zonas", new ZonasPanel());
        agregar("Sectores", new SectoresPanel());
        agregar("Categorías", new CategoriasPanel());
        agregar("Proveedores", new ProveedoresPanel());
    }
}
