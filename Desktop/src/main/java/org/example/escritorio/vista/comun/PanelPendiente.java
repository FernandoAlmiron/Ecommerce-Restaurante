package org.example.escritorio.vista.comun;

import org.example.escritorio.vista.Area;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;

/** Lugar reservado para un área cuya pantalla todavía no se hizo. */
public class PanelPendiente extends JPanel {

    public PanelPendiente(Area area) {
        super(new GridBagLayout());

        JLabel titulo = new JLabel(area.titulo());
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel detalle = new JLabel("Esta pantalla se arma en la fase " + area.fase() + ".");
        detalle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        centro.add(titulo);
        centro.add(Box.createVerticalStrut(8));
        centro.add(detalle);

        add(centro);
    }
}