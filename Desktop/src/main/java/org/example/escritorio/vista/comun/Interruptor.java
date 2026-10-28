package org.example.escritorio.vista.comun;

import javax.swing.JToggleButton;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Interruptor de encendido/apagado (activo o inactivo), café cuando está prendido.
 * Es un JToggleButton: isSelected() dice si está prendido y avisa con addActionListener.
 */
public class Interruptor extends JToggleButton {

    public Interruptor(boolean prendido) {
        super("", prendido);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        putClientProperty("JButton.buttonType", "borderless");
        Dimension tam = new Dimension(52, 30);
        setPreferredSize(tam);
        setMinimumSize(tam);
        setMaximumSize(tam);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Estilo.suave(g);
        if (!isEnabled()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        }
        // siempre del mismo tamaño y centrado, aunque el layout le dé más lugar
        int ancho = Math.min(getWidth(), 52) - 2;
        int alto = Math.min(getHeight(), 30) - 2;
        g2.translate((getWidth() - ancho) / 2, (getHeight() - alto) / 2);
        g2.setColor(isSelected() ? Estilo.CAFE : Estilo.APAGADO);
        g2.fillRoundRect(0, 0, ancho, alto, alto, alto);
        int bolita = alto - 6;
        int x = isSelected() ? ancho - bolita - 3 : 3;
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillOval(x, 4, bolita, bolita);
        g2.setColor(Color.WHITE);
        g2.fillOval(x, 3, bolita, bolita);
        g2.dispose();
    }
}
