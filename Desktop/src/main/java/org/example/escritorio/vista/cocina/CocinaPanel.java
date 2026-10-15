package org.example.escritorio.vista.cocina;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * Área de Cocina: tres solapas, cada una es una pantalla con su propio controlador.
 * Comandas (RF-23, RF-24), Menú y recetas (RF-26 a RF-28) y Stock (RF-30, RF-31).
 */
public class CocinaPanel extends JPanel {

    public CocinaPanel() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titulo = new JLabel("Cocina");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));

        JTabbedPane solapas = new JTabbedPane();
        solapas.addTab("Comandas", new ComandasPanel());
        solapas.addTab("Menú y recetas", new MenuPanel());
        solapas.addTab("Stock", new StockPanel());

        add(titulo, BorderLayout.NORTH);
        add(solapas, BorderLayout.CENTER);
    }
}