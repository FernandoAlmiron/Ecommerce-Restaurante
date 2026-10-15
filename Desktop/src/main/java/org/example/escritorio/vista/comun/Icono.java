package org.example.escritorio.vista.comun;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;

/**
 * Íconos de línea dibujados a mano (no hacen falta imágenes).
 * Si no se le da color, usa el color de texto del componente: así cambia solo al seleccionar.
 */
public class Icono implements Icon {

    public enum Tipo {
        CALENDARIO, MAS_CIRCULO, OLLA, BILLETE, BICI, PERSONAS, ENGRANAJE,
        SALIR, LUPA, ACTUALIZAR, MAS
    }

    private final Tipo tipo;
    private final int tamanio;
    private final Color color;

    public Icono(Tipo tipo, int tamanio) {
        this(tipo, tamanio, null);
    }

    public Icono(Tipo tipo, int tamanio, Color color) {
        this.tipo = tipo;
        this.tamanio = tamanio;
        this.color = color;
    }

    @Override public int getIconWidth() { return tamanio; }
    @Override public int getIconHeight() { return tamanio; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = Estilo.suave(g);
        g2.translate(x, y);
        double k = tamanio / 20.0; // se dibuja en una grilla de 20x20
        g2.scale(k, k);
        g2.setColor(color != null ? color : c.getForeground());
        g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (tipo) {
            case CALENDARIO -> {
                g2.drawRoundRect(2, 4, 16, 14, 4, 4);
                g2.drawLine(2, 8, 18, 8);
                g2.drawLine(6, 2, 6, 5);
                g2.drawLine(14, 2, 14, 5);
            }
            case MAS_CIRCULO -> {
                g2.drawOval(2, 2, 16, 16);
                g2.drawLine(10, 6, 10, 14);
                g2.drawLine(6, 10, 14, 10);
            }
            case OLLA -> {
                g2.drawRoundRect(4, 9, 12, 9, 3, 3);
                g2.drawLine(2, 9, 18, 9);
                g2.drawLine(7, 3, 7, 6);
                g2.drawLine(10, 2, 10, 6);
                g2.drawLine(13, 3, 13, 6);
            }
            case BILLETE -> {
                g2.drawRoundRect(2, 5, 16, 11, 3, 3);
                g2.drawRoundRect(7, 8, 6, 5, 2, 2);
            }
            case BICI -> {
                g2.drawOval(1, 10, 7, 7);
                g2.drawOval(12, 10, 7, 7);
                Path2D cuadro = new Path2D.Float();
                cuadro.moveTo(4.5, 13.5);
                cuadro.lineTo(8, 7);
                cuadro.lineTo(13, 7);
                cuadro.lineTo(15.5, 13.5);
                cuadro.moveTo(8, 7);
                cuadro.lineTo(10.5, 13.5);
                cuadro.lineTo(13, 7);
                cuadro.moveTo(6.5, 4);
                cuadro.lineTo(9, 4);
                g2.draw(cuadro);
            }
            case PERSONAS -> {
                g2.drawOval(4, 3, 6, 6);
                g2.draw(new Arc2D.Float(1.5f, 11, 11, 10, 0, 180, Arc2D.OPEN));
                g2.draw(new Arc2D.Float(11, 3, 5, 6, -90, 180, Arc2D.OPEN));
                g2.draw(new Arc2D.Float(12, 11.5f, 7, 9, 0, 90, Arc2D.OPEN));
            }
            case ENGRANAJE -> {
                g2.drawOval(7, 7, 6, 6);
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI / 4 * i;
                    g2.drawLine((int) Math.round(10 + Math.cos(a) * 6), (int) Math.round(10 + Math.sin(a) * 6),
                            (int) Math.round(10 + Math.cos(a) * 8.5), (int) Math.round(10 + Math.sin(a) * 8.5));
                }
            }
            case SALIR -> {
                Path2D puerta = new Path2D.Float();
                puerta.moveTo(12, 3);
                puerta.lineTo(16, 3);
                puerta.lineTo(16, 17);
                puerta.lineTo(12, 17);
                g2.draw(puerta);
                g2.drawLine(4, 10, 12, 10);
                g2.drawLine(7, 7, 4, 10);
                g2.drawLine(7, 13, 4, 10);
            }
            case LUPA -> {
                g2.drawOval(3, 3, 11, 11);
                g2.drawLine(13, 13, 17, 17);
            }
            case ACTUALIZAR -> {
                g2.draw(new Arc2D.Float(3, 3, 14, 14, 70, 290, Arc2D.OPEN));
                g2.drawLine(13, 2, 13, 5);
                g2.drawLine(13, 5, 16, 5);
            }
            case MAS -> {
                g2.drawLine(10, 3, 10, 17);
                g2.drawLine(3, 10, 17, 10);
            }
        }
        g2.dispose();
    }
}
