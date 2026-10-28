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
        SALIR, LUPA, ACTUALIZAR, MAS, MENOS, USUARIO, CANDADO, OJO, OJO_TACHADO, TILDE, IZQUIERDA, DERECHA, UBICACION, TELEFONO, LAPIZ, COPA, SOBRE, INFO, TACHO, DOCUMENTO, RELOJ, SOL, LUNA
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
            case MENOS -> g2.drawLine(3, 10, 17, 10);
            case COPA -> {
                Path2D copa = new Path2D.Float();
                copa.moveTo(5, 3);
                copa.lineTo(15, 3);
                copa.curveTo(15, 8, 13, 11, 10, 11);
                copa.curveTo(7, 11, 5, 8, 5, 3);
                g2.draw(copa);
                g2.drawLine(10, 11, 10, 17);
                g2.drawLine(7, 17, 13, 17);
            }
            case SOBRE -> {
                g2.drawRoundRect(2, 4, 16, 12, 3, 3);
                Path2D v = new Path2D.Float();
                v.moveTo(3, 5.5);
                v.lineTo(10, 11);
                v.lineTo(17, 5.5);
                g2.draw(v);
            }
            case INFO -> {
                g2.drawOval(2, 2, 16, 16);
                g2.drawLine(10, 9, 10, 14);
                g2.fillOval(9, 5, 2, 2);
            }
            case TACHO -> {
                g2.drawLine(3, 5, 17, 5);
                g2.drawLine(8, 5, 8, 3);
                g2.drawLine(8, 3, 12, 3);
                g2.drawLine(12, 3, 12, 5);
                Path2D balde = new Path2D.Float();
                balde.moveTo(5, 5);
                balde.lineTo(6, 17);
                balde.lineTo(14, 17);
                balde.lineTo(15, 5);
                g2.draw(balde);
                g2.drawLine(8, 8, 8, 14);
                g2.drawLine(12, 8, 12, 14);
            }
            case DOCUMENTO -> {
                g2.drawRoundRect(2, 4, 16, 12, 3, 3);
                g2.drawLine(6, 8, 14, 8);
                g2.drawLine(6, 12, 11, 12);
            }
            case SOL -> {
                g2.drawOval(6, 6, 8, 8);
                for (int i = 0; i < 8; i++) {
                    double angulo = Math.PI / 4 * i;
                    g2.draw(new java.awt.geom.Line2D.Double(10 + 6.2 * Math.cos(angulo), 10 + 6.2 * Math.sin(angulo),
                            10 + 8.2 * Math.cos(angulo), 10 + 8.2 * Math.sin(angulo)));
                }
            }
            case LUNA -> {
                // un círculo al que se le come otro corrido: la medialuna
                java.awt.geom.Area luna = new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(3, 3, 14, 14));
                luna.subtract(new java.awt.geom.Area(new java.awt.geom.Ellipse2D.Double(7.5, 0.5, 12, 12)));
                g2.draw(luna);
            }
            case RELOJ -> {
                g2.drawOval(2, 2, 16, 16);
                g2.drawLine(10, 6, 10, 10);
                g2.drawLine(10, 10, 13, 12);
            }
            case LAPIZ -> {
                Path2D lapiz = new Path2D.Float();
                lapiz.moveTo(4, 16);
                lapiz.lineTo(4.8, 12.6);
                lapiz.lineTo(13, 4.4);
                lapiz.lineTo(15.6, 7);
                lapiz.lineTo(7.4, 15.2);
                lapiz.closePath();
                g2.draw(lapiz);
                g2.drawLine(11, 6, 14, 9);
            }
            case UBICACION -> {
                Path2D gota = new Path2D.Float();
                gota.moveTo(10, 18);
                gota.curveTo(5, 12, 4, 9.5, 4, 8);
                gota.curveTo(4, 4.5, 6.7, 2, 10, 2);
                gota.curveTo(13.3, 2, 16, 4.5, 16, 8);
                gota.curveTo(16, 9.5, 15, 12, 10, 18);
                g2.draw(gota);
                g2.drawOval(8, 6, 4, 4);
            }
            case TELEFONO -> {
                Path2D tel = new Path2D.Float();
                tel.moveTo(5, 3);
                tel.lineTo(8, 3);
                tel.lineTo(9.5, 7);
                tel.lineTo(7.5, 8.5);
                tel.curveTo(8.5, 11, 9, 11.5, 11.5, 12.5);
                tel.lineTo(13, 10.5);
                tel.lineTo(17, 12);
                tel.lineTo(17, 15);
                tel.curveTo(17, 16.5, 16, 17, 15, 17);
                tel.curveTo(9, 16.5, 3.5, 11, 3, 5);
                tel.curveTo(3, 4, 3.5, 3, 5, 3);
                g2.draw(tel);
            }
            case IZQUIERDA -> {
                g2.drawLine(12, 4, 6, 10);
                g2.drawLine(6, 10, 12, 16);
            }
            case DERECHA -> {
                g2.drawLine(8, 4, 14, 10);
                g2.drawLine(14, 10, 8, 16);
            }
            case TILDE -> {
                Path2D tilde = new Path2D.Float();
                tilde.moveTo(3.5, 10.5);
                tilde.lineTo(8, 15);
                tilde.lineTo(16.5, 5);
                g2.draw(tilde);
            }
            case USUARIO -> {
                g2.drawOval(6, 2, 8, 8);
                g2.draw(new Arc2D.Float(3, 12, 14, 12, 0, 180, Arc2D.OPEN));
            }
            case CANDADO -> {
                g2.drawRoundRect(3, 9, 14, 9, 3, 3);
                g2.draw(new Arc2D.Float(6, 3, 8, 10, 0, 180, Arc2D.OPEN));
                g2.drawLine(6, 8, 6, 9);
                g2.drawLine(14, 8, 14, 9);
            }
            case OJO, OJO_TACHADO -> {
                Path2D ojo = new Path2D.Float();
                ojo.moveTo(1.5, 10);
                ojo.quadTo(10, 1, 18.5, 10);
                ojo.quadTo(10, 19, 1.5, 10);
                g2.draw(ojo);
                g2.drawOval(7, 7, 6, 6);
                if (tipo == Tipo.OJO_TACHADO) {
                    g2.drawLine(3, 17, 17, 3);
                }
            }
        }
        g2.dispose();
    }
}
