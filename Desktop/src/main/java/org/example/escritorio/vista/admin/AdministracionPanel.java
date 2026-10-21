package org.example.escritorio.vista.admin;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * Área de Administración (solo ADMIN): la configuración del restaurante.
 * Cada solapa es una pantalla con su propio controlador.
 * Restaurante (RF-49), Zonas (RF-51), Sectores (RF-52), Categorías y Proveedores (RF-56).
 */
public class AdministracionPanel extends JPanel {

    public AdministracionPanel() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titulo = new JLabel("Administración");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));

        JTabbedPane solapas = new JTabbedPane();
        solapas.addTab("Restaurante", new RestaurantePanel());
        solapas.addTab("Zonas", new ZonasPanel());
        solapas.addTab("Sectores", new SectoresPanel());
        solapas.addTab("Categorías", new CategoriasPanel());
        solapas.addTab("Proveedores", new ProveedoresPanel());

        add(titulo, BorderLayout.NORTH);
        add(solapas, BorderLayout.CENTER);
    }
}