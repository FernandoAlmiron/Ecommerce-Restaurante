package org.example.escritorio.vista.comun;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Círculo con las iniciales de una persona ("LB"). Se usa en la tarjeta del usuario logueado,
 * en las listas de personas y en los detalles; se le cambian tamaño y colores según dónde va.
 */
public class Avatar extends JComponent {

    private String iniciales = "";
    private Color fondo = Color.WHITE;
    private Color texto = Estilo.CAFE_OSCURO;

    public Avatar(int diametro, float tamanioLetra) {
        Dimension tam = new Dimension(diametro, diametro);
        setPreferredSize(tam);
        setMinimumSize(tam);
        setMaximumSize(tam);
        setFont(Estilo.fuente(Font.BOLD, tamanioLetra));
    }

    public Avatar(int diametro, float tamanioLetra, String nombreCompleto) {
        this(diametro, tamanioLetra);
        setNombre(nombreCompleto);
    }

    /** Toma las iniciales de las dos primeras palabras: "Laura Benitez" → "LB". */
    public void setNombre(String nombreCompleto) {
        this.iniciales = iniciales(nombreCompleto);
        repaint();
    }

    public void setColores(Color fondo, Color texto) {
        this.fondo = fondo;
        this.texto = texto;
        repaint();
    }

    public static String iniciales(String nombreCompleto) {
        StringBuilder sb = new StringBuilder();
        if (nombreCompleto != null) {
            for (String parte : nombreCompleto.trim().split("\\s+")) {
                if (!parte.isEmpty() && sb.length() < 2) {
                    sb.append(Character.toUpperCase(parte.charAt(0)));
                }
            }
        }
        return sb.toString();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Estilo.suave(g);
        int d = Math.min(getWidth(), getHeight()) - 1;
        int x0 = (getWidth() - d) / 2;
        int y0 = (getHeight() - d) / 2;
        g2.setColor(fondo);
        g2.fillOval(x0, y0, d, d);
        g2.setColor(texto);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(iniciales)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(iniciales, x, y);
        g2.dispose();
    }
}
