package org.example.escritorio.vista.comun;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntSupplier;

/**
 * Tablas con la estética "La Nona": filas altas, solo líneas horizontales, títulos en mayúsculas
 * y etiquetas de color para los estados.
 */
public final class Tablas {

    public static final int RELLENO_CELDA = 18;
    private static final Locale ARGENTINA = Locale.forLanguageTag("es-AR");

    private Tablas() {
    }

    /** Aplica el aspecto general. Las columnas se configuran después con los renderers de acá. */
    public static void estilizar(JTable tabla, int altoFila, Set<Integer> columnasDerecha) {
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(altoFila);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(Estilo.BORDE);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setBackground(Estilo.BLANCO);
        tabla.setForeground(Estilo.TEXTO);
        tabla.setSelectionBackground(Estilo.FILA_SELECCIONADA);
        tabla.setSelectionForeground(Estilo.TEXTO);
        tabla.setFont(Estilo.fuente(Font.PLAIN, 16f));
        tabla.setFocusable(false);
        Formatos.aplicarA(tabla);

        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setReorderingAllowed(false);
        encabezado.setResizingAllowed(false);
        encabezado.setDefaultRenderer(new RendererEncabezado(columnasDerecha));
        encabezado.setBackground(Estilo.ENCABEZADO_TABLA);
    }

    /** Scroll sin bordes, para poner la tabla dentro de una Tarjeta. */
    public static JScrollPane scroll(JTable tabla) {
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        scroll.getViewport().setBackground(Estilo.BLANCO);
        scroll.setBackground(Estilo.BLANCO);
        scroll.setOpaque(false);
        return scroll;
    }

    /** Alto que ocupan el encabezado y todas las filas visibles. */
    public static int altoContenido(JTable tabla) {
        return tabla.getTableHeader().getPreferredSize().height + tabla.getRowCount() * tabla.getRowHeight() + 3;
    }

    public static void renderer(JTable tabla, int columna, TableCellRenderer renderer) {
        tabla.getColumnModel().getColumn(columna).setCellRenderer(renderer);
    }

    public static void anchos(JTable tabla, int... anchos) {
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    /** $ 8.000 (sin centavos si no tiene). */
    public static String pesosCortos(BigDecimal monto) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(ARGENTINA);
        formato.setMinimumFractionDigits(monto.stripTrailingZeros().scale() > 0 ? 2 : 0);
        return formato.format(monto);
    }

    /** 12,5 / 3 (sin decimales si no tiene). */
    public static String numero(double valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(ARGENTINA);
        formato.setMaximumFractionDigits(2);
        return formato.format(valor);
    }

    /** Barra de abajo de una pantalla: a la izquierda un resumen, a la derecha los botones. */
    public static JPanel barraAcciones(JComponent resumen, JComponent... botones) {
        JPanel derecha = Estilo.transparente(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 0));
        for (JComponent b : botones) {
            derecha.add(b);
        }
        JPanel izquierda = Estilo.transparente(new GridBagLayout()); // centrado en alto con los botones
        izquierda.add(resumen);
        JPanel barra = Estilo.transparente(new java.awt.BorderLayout());
        barra.add(izquierda, java.awt.BorderLayout.WEST);
        barra.add(derecha, java.awt.BorderLayout.EAST);
        return barra;
    }

    /** Relleno de la celda; la primera columna de la fila elegida lleva una rayita café a la izquierda. */
    private static Border bordeCelda(JTable tabla, boolean seleccionada, int columnaVista, boolean derecha) {
        int izquierda = derecha ? 8 : RELLENO_CELDA;
        int derechaPx = derecha ? RELLENO_CELDA : 8;
        if (seleccionada && columnaVista == 0) {
            return BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, Estilo.CAFE),
                    Estilo.relleno(0, izquierda - 4, 0, derechaPx));
        }
        return Estilo.relleno(0, izquierda, 0, derechaPx);
    }

    // ---------- renderers ----------

    /** Títulos de columna: mayúsculas chiquitas, grises y con letras separadas. */
    public static class RendererEncabezado extends DefaultTableCellRenderer {
        private final Set<Integer> columnasDerecha;

        public RendererEncabezado(Set<Integer> columnasDerecha) {
            this.columnasDerecha = columnasDerecha;
            setFont(Estilo.conEspaciado(Estilo.fuente(Font.BOLD, 13f), 0.08f));
            setForeground(Estilo.TEXTO_SUAVE);
            setBackground(Estilo.ENCABEZADO_TABLA);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            boolean derecha = columnasDerecha.contains(tabla.convertColumnIndexToModel(columna));
            setText(valor == null ? "" : valor.toString().toUpperCase(ARGENTINA));
            setHorizontalAlignment(derecha ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),
                    Estilo.relleno(20, derecha ? 8 : RELLENO_CELDA, 20, derecha ? RELLENO_CELDA : 8)));
            return this;
        }
    }

    /** Celda de texto con su propia fuente, color y alineación. Si el valor es null muestra "—". */
    public static class RendererTexto extends DefaultTableCellRenderer {
        private final Font fuente;
        private final Color color;
        private final Function<Object, String> texto;
        private final boolean derecha;

        public RendererTexto(Font fuente, Color color, Function<Object, String> texto) {
            this(fuente, color, texto, false);
        }

        public RendererTexto(Font fuente, Color color, Function<Object, String> texto, boolean derecha) {
            this.fuente = fuente;
            this.color = color;
            this.texto = texto;
            this.derecha = derecha;
            setHorizontalAlignment(derecha ? SwingConstants.RIGHT : SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, seleccionada, false, fila, columna);
            setText(valor == null ? "—" : texto.apply(valor));
            setFont(fuente);
            setForeground(color);
            setBorder(bordeCelda(tabla, seleccionada, columna, derecha));
            return this;
        }
    }

    /** Texto, fondo y color de una etiqueta de estado. */
    public record Insignia(String texto, Color fondo, Color color) {
        public static Insignia verde(String texto) { return new Insignia(texto, Estilo.VERDE_FONDO, Estilo.VERDE_TEXTO); }
        public static Insignia amarilla(String texto) { return new Insignia(texto, Estilo.AMARILLO_FONDO, Estilo.AMARILLO_TEXTO); }
        public static Insignia violeta(String texto) { return new Insignia(texto, Estilo.VIOLETA_FONDO, Estilo.VIOLETA_TEXTO); }
        public static Insignia azul(String texto) { return new Insignia(texto, Estilo.AZUL_FONDO, Estilo.AZUL_TEXTO); }
        public static Insignia roja(String texto) { return new Insignia(texto, Estilo.ROJO_FONDO, Estilo.ROJO_TEXTO); }
        public static Insignia gris(String texto) { return new Insignia(texto, Estilo.GRIS_FONDO, Estilo.GRIS_TEXTO); }
    }

    /** Estado como etiqueta redondeada. La función decide texto y colores según el valor de la celda. */
    public static class RendererInsignia implements TableCellRenderer {
        private final JPanel celda = new JPanel(new GridBagLayout());
        private final Etiqueta etiqueta = new Etiqueta();
        private final Function<Object, Insignia> insignia;

        public RendererInsignia(Function<Object, Insignia> insignia) {
            this.insignia = insignia;
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.weightx = 1;
            c.insets = new Insets(0, RELLENO_CELDA, 0, 0);
            celda.add(etiqueta, c);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            celda.setBackground(seleccionada ? tabla.getSelectionBackground() : tabla.getBackground());
            Insignia i = valor == null ? null : insigniaPara(tabla, valor, fila);
            if (i == null) {
                etiqueta.mostrar("", tabla.getBackground(), Estilo.TEXTO);
            } else {
                etiqueta.mostrar(i.texto(), i.fondo(), i.color());
            }
            return celda;
        }

        /** Por defecto depende solo del valor; se puede redefinir si hace falta mirar el resto de la fila. */
        protected Insignia insigniaPara(JTable tabla, Object valor, int filaVista) {
            return insignia.apply(valor);
        }
    }

    private static class Etiqueta extends JLabel {
        private Color fondo = Estilo.GRIS_FONDO;

        Etiqueta() {
            setFont(Estilo.fuente(Font.BOLD, 14f));
            setBorder(Estilo.relleno(5, 14, 5, 14));
        }

        void mostrar(String texto, Color fondo, Color color) {
            setText(texto);
            this.fondo = fondo;
            setForeground(color);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!getText().isEmpty()) {
                Graphics2D g2 = Estilo.suave(g);
                g2.setColor(fondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    // ---------- layout ----------

    /**
     * Apila los hijos de arriba hacia abajo, cada uno con el alto que pide su contenido.
     * Si no entran todos, se reparte el lugar en partes iguales y lo que le sobra a uno se lo lleva otro
     * (dentro de cada tarjeta aparece la barra de desplazamiento de la tabla).
     */
    public static class Apilado implements LayoutManager {
        private final int separacion;
        private final IntSupplier[] altos;

        /** Un proveedor de alto por cada hijo, en el mismo orden en que se agregan. */
        public Apilado(int separacion, IntSupplier... altos) {
            this.separacion = separacion;
            this.altos = altos;
        }

        @Override public void addLayoutComponent(String nombre, Component c) { }
        @Override public void removeLayoutComponent(Component c) { }

        @Override
        public Dimension preferredLayoutSize(Container padre) {
            int total = separacion * Math.max(0, padre.getComponentCount() - 1);
            for (int i = 0; i < padre.getComponentCount(); i++) {
                total += altos[i].getAsInt();
            }
            return new Dimension(0, total);
        }

        @Override
        public Dimension minimumLayoutSize(Container padre) {
            return new Dimension(0, 0);
        }

        @Override
        public void layoutContainer(Container padre) {
            int n = padre.getComponentCount();
            if (n == 0) {
                return;
            }
            int[] deseado = new int[n];
            int[] asignado = new int[n];
            for (int i = 0; i < n; i++) {
                deseado[i] = altos[i].getAsInt();
            }

            // reparto justo: los que piden menos que su parte se llevan lo suyo, el resto se reparte entre los demás
            int disponible = Math.max(0, padre.getHeight() - separacion * (n - 1));
            boolean[] listo = new boolean[n];
            int pendientes = n;
            boolean cambio = true;
            while (cambio && pendientes > 0) {
                cambio = false;
                int parte = disponible / pendientes;
                for (int i = 0; i < n; i++) {
                    if (!listo[i] && deseado[i] <= parte) {
                        asignado[i] = deseado[i];
                        disponible -= deseado[i];
                        listo[i] = true;
                        pendientes--;
                        cambio = true;
                    }
                }
            }
            if (pendientes > 0) {
                int parte = disponible / pendientes;
                for (int i = 0; i < n; i++) {
                    if (!listo[i]) {
                        asignado[i] = parte;
                    }
                }
            }

            int y = 0;
            for (int i = 0; i < n; i++) {
                padre.getComponent(i).setBounds(0, y, padre.getWidth(), asignado[i]);
                y += asignado[i] + separacion;
            }
        }
    }
}
