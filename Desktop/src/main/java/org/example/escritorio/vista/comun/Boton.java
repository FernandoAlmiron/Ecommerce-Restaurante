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

    /** Los colores se piden a Estilo cada vez (no se guardan acá), así siguen al tema claro u oscuro. */
    public enum Tipo {
        /** Café con texto blanco (acción principal de la pantalla). */
        PRIMARIO,
        /** Casi negro con texto blanco (acción principal de una barra). */
        OSCURO,
        /** Blanco con borde y texto oscuro. */
        CONTORNO,
        /** Blanco con borde y texto café (acciones que deshacen algo). */
        CONTORNO_CAFE;

        Color fondo() {
            return switch (this) {
                case PRIMARIO -> Estilo.CAFE;
                case OSCURO -> Estilo.OSCURO;
                case CONTORNO, CONTORNO_CAFE -> Estilo.BLANCO;
            };
        }

        Color fondoHover() {
            return switch (this) {
                case PRIMARIO -> Estilo.CAFE_HOVER;
                case OSCURO -> Estilo.OSCURO_HOVER;
                case CONTORNO, CONTORNO_CAFE -> Estilo.FONDO;
            };
        }

        Color texto() {
            return switch (this) {
                case PRIMARIO, OSCURO -> Color.WHITE;
                case CONTORNO -> Estilo.TEXTO;
                case CONTORNO_CAFE -> Estilo.CAFE;
            };
        }

        /** null = sin borde. */
        Color borde() {
            return this == CONTORNO || this == CONTORNO_CAFE ? Estilo.BORDE : null;
        }
    }

    private static final float OPACIDAD_DESHABILITADO = 0.5f;

    private final Tipo tipo;
    private int arco = 14;

    public Boton(String texto, Tipo tipo) {
        this(texto, null, tipo);
    }

    /** Botón cuadrado, blanco y con borde, que solo tiene un ícono (editar, borrar...); el texto va de ayuda. */
    public static Boton soloIcono(Icono.Tipo icono, Color color, String ayuda) {
        Boton b = new Boton(null, new Icono(icono, 18, color), Tipo.CONTORNO);
        b.setBorder(Estilo.relleno(10, 10, 10, 10));
        b.setToolTipText(ayuda);
        return b;
    }

    public Boton(String texto, Icon icono, Tipo tipo) {
        super(texto, icono);
        this.tipo = tipo;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setForeground(tipo.texto());
        setFont(Estilo.fuente(tipo == Tipo.PRIMARIO || tipo == Tipo.OSCURO ? Font.BOLD : Font.PLAIN, 15f));
        setIconTextGap(10);
        setMargin(new java.awt.Insets(12, 22, 12, 22));
        setBorder(Estilo.relleno(12, 22, 12, 22));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // FlatLaf no tiene que pintar nada propio sobre este botón
        putClientProperty("JButton.buttonType", "borderless");
        // deshabilitado se ve igual pero transparente (ver paintComponent), no con el texto gris de FlatLaf
        Estilo.flat(this, "disabledText: " + Estilo.hex(tipo.texto()));
    }

    public Boton conArco(int arco) {
        this.arco = arco;
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Estilo.suave(g);
        if (!isEnabled()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OPACIDAD_DESHABILITADO));
        }
        boolean encima = isEnabled() && (getModel().isRollover() || getModel().isPressed());
        g2.setColor(encima ? tipo.fondoHover() : tipo.fondo());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
        if (tipo.borde() != null) {
            g2.setColor(tipo.borde());
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
        }
        g2.dispose();
        // el texto y el ícono los pinta Swing, con la misma transparencia que el fondo
        Graphics2D gTexto = (Graphics2D) g.create();
        if (!isEnabled()) {
            gTexto.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OPACIDAD_DESHABILITADO));
        }
        super.paintComponent(gTexto);
        gTexto.dispose();
    }
}
