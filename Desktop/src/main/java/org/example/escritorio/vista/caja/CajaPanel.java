package org.example.escritorio.vista.caja;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * Área de Caja: cuatro solapas, cada una con su controlador.
 * Cobros (RF-40 a RF-45), Cierre de caja (RF-47), Reporte de delivery (RF-48) y Tarjetas (RF-46).
 */
public class CajaPanel extends JPanel {

    public CajaPanel() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titulo = new JLabel("Caja");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));

        JTabbedPane solapas = new JTabbedPane();
        solapas.addTab("Cobros", new CobrosPanel());
        solapas.addTab("Cierre de caja", new CierreCajaPanel());
        solapas.addTab("Reporte de delivery", new ReporteDeliveryPanel());

        add(titulo, BorderLayout.NORTH);
        add(solapas, BorderLayout.CENTER);
    }
}