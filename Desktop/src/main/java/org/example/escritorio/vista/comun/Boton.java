package org.example.escritorio.vista.comun;

import javax.swing.Icon;
import javax.swing.JButton;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Botón redondeado de la estética "La Nona". El tipo define sus colores. */
public class Boton extends JButton {

    public enum Tipo {
        /** Café con texto blanco (acción principal de la pantalla). */
        PRIMARIO(Estilo.CAFE, Estilo.CAFE_HOVER, Color.WHITE, null),
        /** Casi negro con texto blanco (acción principal de una barra). */
        OSCURO(Estilo.OSCURO, Estilo.OSCURO_HOVER, Color.WHITE, null),
        /** Blanco con borde y texto oscuro. */
        CONTORNO(Color.WHITE, Estilo.FONDO, Estilo.TEXTO, Estilo.BORDE),
        /** Blanco con borde y texto café (acciones que deshacen algo). */
        CONTORNO_CAFE(Color.WHITE, Estilo.FONDO, Estilo.CAFE, Estilo.BORDE);

        final Color fondo;
        final Color fondoHover;
        final Color texto;
        final Color borde;

        Tipo(Color fondo, Color fondoHover, Color texto, Color borde) {
            this.fondo = fondo;
            this.fondoHover = fondoHover;
            this.texto = texto;
            this.borde = borde;
        }
    }

    private final Tipo tipo;
    private int arco = 14;

    public Boton(String texto, Tipo tipo) {
        this(texto, null, tipo);
    }

    public Boton(String texto, Icon icono, Tipo tipo) {
        super(texto, icono);
        this.tipo = tipo;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setForeground(tipo.texto);
        setFont(Estilo.fuente(tipo == Tipo.PRIMARIO || tipo == Tipo.OSCURO ? Font.BOLD : Font.PLAIN, 15f));
        setIconTextGap(10);
        setMargin(new java.awt.Insets(12, 22, 12, 22));
        setBorder(Estilo.relleno(12, 22, 12, 22));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // FlatLaf no tiene que pintar nada propio sobre este botón
        putClientProperty("JButton.buttonType", "borderless");
    }

    public Boton conArco(int arco) {
        this.arco = arco;
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Estilo.suave(g);
        if (!isEnabled()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        }
        boolean encima = isEnabled() && (getModel().isRollover() || getModel().isPressed());
        g2.setColor(encima ? tipo.fondoHover : tipo.fondo);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
        if (tipo.borde != null) {
            g2.setColor(tipo.borde);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
        }
        g2.dispose();
        // el texto y el ícono los pinta Swing (deshabilitado, en gris)
        super.paintComponent(g);
    }
}
