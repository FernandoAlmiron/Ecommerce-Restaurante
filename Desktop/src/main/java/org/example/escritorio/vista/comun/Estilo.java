package org.example.escritorio.vista.comun;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Colores, fuentes y piezas visuales de la estética "La Nona" (café, crema y blanco).
 * Las pantallas toman todo de acá para que se vean iguales.
 */
public final class Estilo {

    // ---------- colores ----------
    public static final Color CAFE = new Color(0x8C5E35);
    public static final Color CAFE_HOVER = new Color(0x7A5029);
    public static final Color CAFE_OSCURO = new Color(0x6B4524);
    public static final Color FONDO = new Color(0xF4F2EF);
    public static final Color BLANCO = Color.WHITE;
    public static final Color BORDE = new Color(0xE9E4DE);
    public static final Color TEXTO = new Color(0x2A2420);
    public static final Color TEXTO_SUAVE = new Color(0x7D746C);
    public static final Color ENCABEZADO_TABLA = new Color(0xFAF8F6);
    public static final Color FILA_SELECCIONADA = new Color(0xF7ECE1);
    public static final Color OSCURO = new Color(0x2B2421);
    public static final Color OSCURO_HOVER = new Color(0x3E3530);
    public static final Color SEGMENTO_FONDO = new Color(0xF1EEEA);

    public static final Color VERDE_FONDO = new Color(0xE4F2E6);
    public static final Color VERDE_TEXTO = new Color(0x2E7D32);
    public static final Color AMARILLO_FONDO = new Color(0xFDF0CC);
    public static final Color AMARILLO_TEXTO = new Color(0x97600A);
    public static final Color ROJO_FONDO = new Color(0xFBE3E0);
    public static final Color ROJO_TEXTO = new Color(0xB3261E);
    public static final Color VIOLETA_FONDO = new Color(0xECE8FA);
    public static final Color VIOLETA_TEXTO = new Color(0x5B45A8);
    public static final Color GRIS_FONDO = new Color(0xEDE8E4);
    public static final Color GRIS_TEXTO = new Color(0x6E625A);

    private static final Set<String> FUENTES_INSTALADAS = Arrays
            .stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
            .collect(Collectors.toSet());

    private static final String FUENTE_BASE = primeraInstalada("Segoe UI", "Helvetica Neue", "Arial", Font.SANS_SERIF);

    private Estilo() {
    }

    // ---------- fuentes ----------

    public static Font fuente(int estilo, float tamanio) {
        return new Font(FUENTE_BASE, estilo, Math.round(tamanio));
    }

    /** Fuente cursiva del logo; si la PC no tiene ninguna manuscrita, una serif en itálica. */
    public static Font fuenteLogo(float tamanio) {
        String familia = primeraInstalada("Segoe Script", "Brush Script MT", "Lucida Handwriting", null);
        if (familia == null) {
            return new Font(Font.SERIF, Font.BOLD | Font.ITALIC, Math.round(tamanio));
        }
        return new Font(familia, Font.BOLD, Math.round(tamanio));
    }

    public static Font fuenteSerif(float tamanio) {
        return new Font(primeraInstalada("Georgia", "Times New Roman", Font.SERIF), Font.PLAIN, Math.round(tamanio));
    }

    /** Mayúsculas con letras separadas, como los títulos de columna. */
    public static Font conEspaciado(Font fuente, float espaciado) {
        return fuente.deriveFont(Map.of(TextAttribute.TRACKING, espaciado));
    }

    private static String primeraInstalada(String... familias) {
        for (String f : familias) {
            if (f == null || FUENTES_INSTALADAS.contains(f) || f.equals(Font.SANS_SERIF) || f.equals(Font.SERIF)) {
                return f;
            }
        }
        return Font.SANS_SERIF;
    }

    // ---------- dibujo ----------

    public static Graphics2D suave(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    public static Border relleno(int arriba, int izquierda, int abajo, int derecha) {
        return BorderFactory.createEmptyBorder(arriba, izquierda, abajo, derecha);
    }

    /** Panel transparente (deja ver el fondo del que lo contiene). */
    public static JPanel transparente(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        return p;
    }

    /** Tarjeta blanca con esquinas redondeadas y un borde finito. */
    public static class Tarjeta extends JPanel {
        private final int arco;

        public Tarjeta(LayoutManager layout, int arco) {
            super(layout);
            this.arco = arco;
            setOpaque(false);
            setBackground(BLANCO);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
            g2.dispose();
        }

        @Override
        protected void paintChildren(Graphics g) {
            // los hijos se recortan a la forma redondeada (por ejemplo, la tabla en las esquinas)
            Graphics2D g2 = suave(g);
            g2.clip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arco, arco));
            super.paintChildren(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = suave(g);
            g2.setColor(BORDE);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
            g2.dispose();
        }
    }

    /** Miga ("Recepción · Jueves 15 de octubre") y título grande de una pantalla. */
    public static JPanel tituloPantalla(String area, String titulo) {
        String dia = java.time.format.DateTimeFormatter
                .ofPattern("EEEE d 'de' MMMM", java.util.Locale.forLanguageTag("es-AR"))
                .format(java.time.LocalDate.now());
        javax.swing.JLabel miga = new javax.swing.JLabel(
                area + " · " + Character.toUpperCase(dia.charAt(0)) + dia.substring(1));
        miga.setFont(fuente(Font.PLAIN, 15f));
        miga.setForeground(TEXTO_SUAVE);

        javax.swing.JLabel grande = new javax.swing.JLabel(titulo);
        grande.setFont(fuente(Font.BOLD, 34f));
        grande.setForeground(TEXTO);

        JPanel textos = transparente(null);
        textos.setLayout(new javax.swing.BoxLayout(textos, javax.swing.BoxLayout.Y_AXIS));
        textos.add(miga);
        textos.add(javax.swing.Box.createVerticalStrut(4));
        textos.add(grande);
        return textos;
    }

    /** Texto sobre un fondo redondeado (cantidades, observaciones, contadores). */
    public static class Pildora extends javax.swing.JLabel {
        private Color fondo;
        private final int arco;

        public Pildora(String texto, Color fondo, Color color, Font fuente, int arco) {
            super(texto);
            this.fondo = fondo;
            this.arco = arco;
            setForeground(color);
            setFont(fuente);
            setBorder(relleno(4, 10, 4, 10));
        }

        public void setFondo(Color fondo) {
            this.fondo = fondo;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            g2.setColor(fondo);
            int a = arco < 0 ? getHeight() : arco;
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), a, a);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** La rayita decorativa del logo: un palo fino con un rodillo blanco en el medio. */
    public static class Rodillo extends JComponent {
        private final int largoPalo;
        private final int largoRodillo;
        private final int grosor;

        public Rodillo(int largoPalo, int largoRodillo, int grosor) {
            this.largoPalo = largoPalo;
            this.largoRodillo = largoRodillo;
            this.grosor = grosor;
            java.awt.Dimension tam = new java.awt.Dimension(largoPalo, grosor + 6);
            setPreferredSize(tam);
            setMaximumSize(tam);
            setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            g2.setColor(new Color(255, 255, 255, 200));
            g2.setStroke(new BasicStroke(Math.max(2f, grosor / 4f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(cx - largoPalo / 2 + 2, cy, cx + largoPalo / 2 - 2, cy);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(cx - largoRodillo / 2, cy - grosor / 2, largoRodillo, grosor, grosor, grosor);
            g2.dispose();
        }
    }

    /**
     * Casilla café con tilde blanco. Se fijan los colores de todos los estados (con foco, con el mouse
     * encima, apretada): si no, FlatLaf usa los suyos y con foco el tilde blanco queda sobre fondo blanco,
     * invisible hasta pasar el mouse.
     */
    public static void casilla(javax.swing.JCheckBox casilla) {
        String cafe = "#8C5E35";
        String cafeOscuro = "#7A5029";
        flat(casilla, "icon.focusWidth: 0;"
                + " icon.checkmarkColor: #FFFFFF; icon.focusedCheckmarkColor: #FFFFFF;"
                + " icon.hoverCheckmarkColor: #FFFFFF; icon.pressedCheckmarkColor: #FFFFFF;"
                + " icon.selectedBackground: " + cafe + "; icon.selectedBorderColor: " + cafe + ";"
                + " icon.focusedSelectedBackground: " + cafe + "; icon.focusedSelectedBorderColor: " + cafe + ";"
                + " icon.hoverSelectedBackground: " + cafeOscuro + "; icon.hoverSelectedBorderColor: " + cafeOscuro + ";"
                + " icon.pressedSelectedBackground: " + cafeOscuro + "; icon.pressedSelectedBorderColor: " + cafeOscuro + ";"
                + " icon.focusedBackground: #FFFFFF; icon.focusedBorderColor: " + cafe + ";"
                + " icon.hoverBorderColor: " + cafe);
    }

    /** Le pone al componente el estilo de FlatLaf (si FlatLaf no está, se ignora). */
    public static void flat(JComponent c, String estilo) {
        c.putClientProperty("FlatLaf.style", estilo);
    }
}
