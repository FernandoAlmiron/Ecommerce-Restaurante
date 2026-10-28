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
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

/**
 * Colores, fuentes y piezas visuales de la estética "La Nona" (café, crema y blanco), en tema claro u oscuro.
 * Las pantallas toman todo de acá para que se vean iguales.
 */
public final class Estilo {

    // ---------- colores ----------
    // Cambian con el tema (claro u oscuro), por eso no son final: las pantallas los leen al armarse
    // y, al cambiar de tema, la ventana se vuelve a armar (ver usarTema).
    // BLANCO es el fondo de tarjetas, tablas y diálogos: en el tema oscuro es un gris café oscuro.
    // Lo que va sobre café (texto de los botones principales, la barra lateral) es Color.WHITE en los dos.
    public static Color CAFE;
    public static Color CAFE_HOVER;
    /** Café más fuerte para números grandes (totales, capacidad); en oscuro, un dorado que se lea. */
    public static Color CAFE_OSCURO;
    public static Color FONDO;
    public static Color BLANCO;
    public static Color BORDE;
    public static Color TEXTO;
    public static Color TEXTO_SUAVE;
    public static Color ENCABEZADO_TABLA;
    public static Color FILA_SELECCIONADA;
    public static Color OSCURO;
    public static Color OSCURO_HOVER;
    public static Color SEGMENTO_FONDO;
    /** La opción elegida de un Selector (sobre SEGMENTO_FONDO). */
    public static Color SEGMENTO_ELEGIDO;

    // barra lateral y mitad del logo en el login
    public static Color MENU_FONDO;
    public static Color MENU_TARJETA;
    public static Color MENU_ELEGIDO;
    public static Color MENU_ELEGIDO_TEXTO;

    // detalles
    public static Color PUNTEADO;
    /** Interruptor apagado y barras vacías. */
    public static Color APAGADO;
    public static Color BORDE_CAMPO;
    public static Color FOCO;

    public static Color VERDE_FONDO;
    public static Color VERDE_TEXTO;
    public static Color AMARILLO_FONDO;
    public static Color AMARILLO_TEXTO;
    public static Color ROJO_FONDO;
    public static Color ROJO_TEXTO;
    public static Color AZUL_FONDO;
    public static Color AZUL_TEXTO;
    public static Color VIOLETA_FONDO;
    public static Color VIOLETA_TEXTO;
    public static Color GRIS_FONDO;
    public static Color GRIS_TEXTO;

    private static final Preferences PREFERENCIAS = Preferences.userNodeForPackage(Estilo.class);
    private static final String CLAVE_TEMA = "temaOscuro";
    private static boolean oscuro;

    /** Fondos de FlatLaf que en el tema oscuro se igualan a las tarjetas (BLANCO). */
    private static final String[] CLAVES_SUPERFICIE = {"Panel.background", "OptionPane.background",
            "TextField.background", "PasswordField.background", "FormattedTextField.background", "TextArea.background",
            "TextPane.background", "EditorPane.background", "Spinner.background", "ComboBox.background",
            "ComboBox.buttonBackground", "ComboBox.popupBackground", "List.background", "Table.background",
            "Viewport.background", "ScrollPane.background", "PopupMenu.background", "ToolTip.background"};
    /** Lo elegido en listas, tablas y textos: café en el tema oscuro. */
    private static final String[] CLAVES_SELECCION = {"List.selectionBackground", "ComboBox.selectionBackground",
            "Table.selectionBackground", "TextField.selectionBackground", "TextArea.selectionBackground"};

    static {
        aplicarPaleta(false);
    }

    private static final Set<String> FUENTES_INSTALADAS = Arrays
            .stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
            .collect(Collectors.toSet());

    private static final String FUENTE_BASE = primeraInstalada("Segoe UI", "Helvetica Neue", "Arial", Font.SANS_SERIF);

    private Estilo() {
    }

    // ---------- tema ----------

    /** Los colores de cada tema. El oscuro usa los mismos tonos que la web (style.css, [data-tema="oscuro"]). */
    private static void aplicarPaleta(boolean oscuro) {
        if (!oscuro) {
            CAFE = new Color(0x8C5E35);
            CAFE_HOVER = new Color(0x7A5029);
            CAFE_OSCURO = new Color(0x6B4524);
            FONDO = new Color(0xF4F2EF);
            BLANCO = Color.WHITE;
            BORDE = new Color(0xE9E4DE);
            TEXTO = new Color(0x2A2420);
            TEXTO_SUAVE = new Color(0x7D746C);
            ENCABEZADO_TABLA = new Color(0xFAF8F6);
            FILA_SELECCIONADA = new Color(0xF7ECE1);
            OSCURO = new Color(0x2B2421);
            OSCURO_HOVER = new Color(0x3E3530);
            SEGMENTO_FONDO = new Color(0xF1EEEA);
            SEGMENTO_ELEGIDO = Color.WHITE;
            MENU_FONDO = new Color(0x8C5E35);
            MENU_TARJETA = new Color(0x6B4524);
            MENU_ELEGIDO = Color.WHITE;
            MENU_ELEGIDO_TEXTO = new Color(0x8C5E35);
            PUNTEADO = new Color(0xD9CBBE);
            APAGADO = new Color(0xE2DAD2);
            BORDE_CAMPO = new Color(0xE0D6CC);
            FOCO = new Color(0xEFE7DF);
            VERDE_FONDO = new Color(0xE4F2E6);
            VERDE_TEXTO = new Color(0x2E7D32);
            AMARILLO_FONDO = new Color(0xFDF0CC);
            AMARILLO_TEXTO = new Color(0x97600A);
            ROJO_FONDO = new Color(0xFBE3E0);
            ROJO_TEXTO = new Color(0xB3261E);
            AZUL_FONDO = new Color(0xE3EEFB);
            AZUL_TEXTO = new Color(0x1F5FA8);
            VIOLETA_FONDO = new Color(0xECE8FA);
            VIOLETA_TEXTO = new Color(0x5B45A8);
            GRIS_FONDO = new Color(0xEDE8E4);
            GRIS_TEXTO = new Color(0x6E625A);
        } else {
            CAFE = new Color(0xA9744A);
            CAFE_HOVER = new Color(0x93633C);
            CAFE_OSCURO = new Color(0xD9B46A);
            FONDO = new Color(0x141210);
            BLANCO = new Color(0x1E1B18);
            BORDE = new Color(0x342C25);
            TEXTO = new Color(0xECE6DD);
            TEXTO_SUAVE = new Color(0xA59B91);
            ENCABEZADO_TABLA = new Color(0x25211D);
            FILA_SELECCIONADA = new Color(0x362B21);
            OSCURO = new Color(0x4A3F37);
            OSCURO_HOVER = new Color(0x5A4D44);
            SEGMENTO_FONDO = new Color(0x2A241E);
            SEGMENTO_ELEGIDO = new Color(0x423830);
            MENU_FONDO = new Color(0x2B2019);
            MENU_TARJETA = new Color(0x3D2D21);
            MENU_ELEGIDO = new Color(0x4D3929);
            MENU_ELEGIDO_TEXTO = new Color(0xF1DDBF);
            PUNTEADO = new Color(0x4A4036);
            APAGADO = new Color(0x4A4036);
            BORDE_CAMPO = new Color(0x4A4036);
            FOCO = new Color(0x3A3027);
            VERDE_FONDO = new Color(0x1F3323);
            VERDE_TEXTO = new Color(0x86CF8C);
            AMARILLO_FONDO = new Color(0x3A2F14);
            AMARILLO_TEXTO = new Color(0xE8B84E);
            ROJO_FONDO = new Color(0x3D1E1B);
            ROJO_TEXTO = new Color(0xF08A80);
            AZUL_FONDO = new Color(0x1B2A3D);
            AZUL_TEXTO = new Color(0x8AB6F0);
            VIOLETA_FONDO = new Color(0x2A2440);
            VIOLETA_TEXTO = new Color(0xB3A3F0);
            GRIS_FONDO = new Color(0x2E2924);
            GRIS_TEXTO = new Color(0xB3A79C);
        }
    }

    public static boolean esOscuro() {
        return oscuro;
    }

    /**
     * Pone el tema: la paleta de acá y el aspecto de FlatLaf (listas desplegables, barras de desplazamiento,
     * mensajes). Las ventanas que ya están abiertas no cambian solas: hay que volver a armarlas.
     */
    public static void usarTema(boolean oscuro) {
        Estilo.oscuro = oscuro;
        aplicarPaleta(oscuro);
        // lo que se pone con UIManager.put sobrevive al cambio de aspecto: se borra antes de cada cambio
        for (String clave : CLAVES_SUPERFICIE) {
            UIManager.put(clave, null);
        }
        for (String clave : CLAVES_SELECCION) {
            UIManager.put(clave, null);
        }
        UIManager.put("Component.borderColor", null);
        UIManager.put("Component.focusedBorderColor", null);
        UIManager.put("ScrollBar.track", null);
        try {
            UIManager.setLookAndFeel(oscuro ? "com.formdev.flatlaf.FlatDarkLaf" : "com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception sinFlatLaf) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorado) {
                // queda el aspecto por defecto de Swing
            }
            return;
        }
        if (oscuro) {
            // los componentes de FlatLaf (campos, listas, tablas) con los mismos fondos que las tarjetas
            ColorUIResource superficie = new ColorUIResource(BLANCO);
            for (String clave : CLAVES_SUPERFICIE) {
                UIManager.put(clave, superficie);
            }
            UIManager.put("Component.borderColor", new ColorUIResource(BORDE_CAMPO));
            UIManager.put("Component.focusedBorderColor", new ColorUIResource(CAFE));
            UIManager.put("ScrollBar.track", new ColorUIResource(BLANCO));
            for (String clave : CLAVES_SELECCION) {
                UIManager.put(clave, new ColorUIResource(CAFE));
            }
        }
    }

    /** El tema que se eligió la última vez en esta PC (el claro si nunca se eligió). */
    public static boolean temaGuardadoEsOscuro() {
        try {
            return PREFERENCIAS.getBoolean(CLAVE_TEMA, false);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Pasa al otro tema y lo recuerda para la próxima vez que se abra la aplicación en esta PC. */
    public static void cambiarTema() {
        usarTema(!oscuro);
        try {
            PREFERENCIAS.putBoolean(CLAVE_TEMA, oscuro);
        } catch (RuntimeException ignorado) {
            // si no se puede guardar, igual cambia por ahora
        }
    }

    /** El color en el formato de los estilos de FlatLaf ("#8C5E35"). */
    public static String hex(Color color) {
        return String.format("#%06X", color.getRGB() & 0xFFFFFF);
    }

    /** Borde de los campos para el estilo de FlatLaf, con los colores del tema. */
    public static String bordeCampo(int anchoFoco) {
        return "focusWidth: " + anchoFoco + "; innerFocusWidth: 0; focusColor: " + hex(FOCO) + ";"
                + " borderColor: " + hex(BORDE_CAMPO) + "; focusedBorderColor: " + hex(CAFE);
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

    /** Borde de una línea punteada arriba o abajo, para separar partes como en un ticket de papel. */
    public static class LineaPunteada implements javax.swing.border.Border {
        private final boolean arriba;

        public LineaPunteada(boolean arriba) {
            this.arriba = arriba;
        }

        @Override
        public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int ancho, int alto) {
            Graphics2D g2 = suave(g);
            g2.setColor(PUNTEADO);
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
                    new float[]{4f, 4f}, 0f));
            int yLinea = arriba ? y : y + alto - 1;
            g2.drawLine(x, yLinea, x + ancho, yLinea);
            g2.dispose();
        }

        @Override
        public java.awt.Insets getBorderInsets(java.awt.Component c) {
            return arriba ? new java.awt.Insets(1, 0, 0, 0) : new java.awt.Insets(0, 0, 1, 0);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
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
        String cafe = hex(CAFE);
        String cafeOscuro = hex(CAFE_HOVER);
        flat(casilla, "icon.focusWidth: 0;"
                + " icon.checkmarkColor: #FFFFFF; icon.focusedCheckmarkColor: #FFFFFF;"
                + " icon.hoverCheckmarkColor: #FFFFFF; icon.pressedCheckmarkColor: #FFFFFF;"
                + " icon.selectedBackground: " + cafe + "; icon.selectedBorderColor: " + cafe + ";"
                + " icon.focusedSelectedBackground: " + cafe + "; icon.focusedSelectedBorderColor: " + cafe + ";"
                + " icon.hoverSelectedBackground: " + cafeOscuro + "; icon.hoverSelectedBorderColor: " + cafeOscuro + ";"
                + " icon.pressedSelectedBackground: " + cafeOscuro + "; icon.pressedSelectedBorderColor: " + cafeOscuro + ";"
                + " icon.focusedBackground: " + hex(BLANCO) + "; icon.focusedBorderColor: " + cafe + ";"
                + " icon.hoverBorderColor: " + cafe);
    }

    /** Le pone al componente el estilo de FlatLaf (si FlatLaf no está, se ignora). */
    public static void flat(JComponent c, String estilo) {
        c.putClientProperty("FlatLaf.style", estilo);
    }
}
