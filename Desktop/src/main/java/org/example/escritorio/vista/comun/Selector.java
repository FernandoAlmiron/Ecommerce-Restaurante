package org.example.escritorio.vista.comun;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Opciones de las que se elige una sola, como pestañas chicas sobre un fondo gris claro
 * ("Todas / Pendientes / Confirmadas", "Por cobrar / Todos", "Hoy / Ayer").
 * La primera opción empieza elegida; al cambiar se avisa el índice elegido.
 */
public class Selector extends JPanel {

    private final List<Opcion> opciones = new ArrayList<>();
    private final ButtonGroup grupo = new ButtonGroup();
    private final List<IntConsumer> oyentes = new ArrayList<>();

    public Selector(String... textos) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);
        setBorder(Estilo.relleno(4, 4, 4, 4));
        setOpciones(textos);
    }

    /**
     * Rearma las opciones (por ejemplo, cuando dependen de los datos cargados). Queda elegida la primera;
     * los oyentes se mantienen.
     */
    public void setOpciones(String... textos) {
        for (Opcion o : opciones) {
            grupo.remove(o);
        }
        opciones.clear();
        removeAll();
        for (int i = 0; i < textos.length; i++) {
            Opcion o = new Opcion(textos[i]);
            int indice = i;
            o.addActionListener(e -> oyentes.forEach(oyente -> oyente.accept(indice)));
            grupo.add(o);
            opciones.add(o);
            add(o);
        }
        if (!opciones.isEmpty()) {
            opciones.get(0).setSelected(true);
        }
        revalidate();
        repaint();
    }

    public int cantidad() {
        return opciones.size();
    }

    /** Se llama con el índice de la opción cada vez que el usuario elige una. */
    public void alElegir(IntConsumer oyente) {
        oyentes.add(oyente);
    }

    public int getElegida() {
        for (int i = 0; i < opciones.size(); i++) {
            if (opciones.get(i).isSelected()) {
                return i;
            }
        }
        return -1;
    }

    /** Elige una opción desde el código (no avisa a los oyentes). */
    public void elegir(int indice) {
        opciones.get(indice).setSelected(true);
    }

    /** Cambia el texto de una opción (por ejemplo, para mostrar cuántos hay: "Pendientes · 3"). */
    public void setTexto(int indice, String texto) {
        opciones.get(indice).setText(texto);
        revalidate();
        repaint();
    }

    /** Deja todas las opciones sin elegir (por ejemplo, cuando la fecha no es ni hoy ni ayer). */
    public void limpiar() {
        grupo.clearSelection();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Estilo.suave(g);
        g2.setColor(Estilo.SEGMENTO_FONDO);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        g2.dispose();
    }

    /** Una opción: blanca y en negrita cuando está elegida. */
    private static class Opcion extends JToggleButton {
        Opcion(String texto) {
            super(texto);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(Estilo.relleno(10, 20, 10, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            putClientProperty("JButton.buttonType", "borderless");
            getModel().addChangeListener(e -> {
                setForeground(isSelected() ? Estilo.TEXTO : Estilo.TEXTO_SUAVE);
                setFont(Estilo.fuente(isSelected() ? Font.BOLD : Font.PLAIN, 16f));
            });
            setForeground(Estilo.TEXTO_SUAVE);
            setFont(Estilo.fuente(Font.PLAIN, 16f));
        }

        @Override
        public Dimension getPreferredSize() {
            // mismo ancho elegida o no (la negrita no tiene que mover a las demás)
            FontMetrics fm = getFontMetrics(Estilo.fuente(Font.BOLD, 16f));
            Insets i = getInsets();
            return new Dimension(fm.stringWidth(getText()) + i.left + i.right + 2,
                    fm.getHeight() + i.top + i.bottom);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (isSelected()) {
                Graphics2D g2 = Estilo.suave(g);
                g2.setColor(new Color(0, 0, 0, 14));
                g2.fillRoundRect(0, 1, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(Estilo.SEGMENTO_ELEGIDO);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 2, 12, 12);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
