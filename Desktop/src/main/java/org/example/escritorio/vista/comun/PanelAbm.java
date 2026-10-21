package org.example.escritorio.vista.comun;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Pantalla base para un ABM (alta, baja y modificación) con la estética "La Nona".
 * Arriba un resumen (o filtros), Actualizar y el botón de alta; abajo la lista de lo cargado.
 *
 * Cada pantalla concreta (zonas, sectores, proveedores...) la extiende y solo define cómo se ve
 * cada elemento en {@link #crearFila}. Si quiere, suma un panel de detalle a la derecha
 * ({@link #armarDetalle} / {@link #mostrarDetalle}), filtros ({@link #armarFiltros} / {@link #incluir})
 * o una nota debajo de la lista ({@link #armarNota}). Los botones se conectan con el controlador en
 * {@link #conectar}, igual que antes.
 *
 * Hay dos formas de mostrar la lista:
 *  - LISTA: filas separadas por una línea, dentro de una o más tarjetas (columnas).
 *  - TARJETAS: cada elemento es su propia tarjeta, en una grilla.
 */
public abstract class PanelAbm<T> extends JPanel {

    public enum Disposicion { LISTA, TARJETAS }

    private final int columnas;
    private final Disposicion disposicion;
    private final List<T> filas = new ArrayList<>();
    private T elegido;

    private final JButton btnActualizar = new Boton(null, new Icono(Icono.Tipo.ACTUALIZAR, 20, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);
    private final JButton btnNuevo;
    private final JButton btnModificar = new Boton("Modificar", new Icono(Icono.Tipo.LAPIZ, 18, Color.WHITE),
            Boton.Tipo.PRIMARIO);
    private final JButton btnEliminar = new Boton("Eliminar", Boton.Tipo.CONTORNO_CAFE);
    private final JPanel botonesExtra = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 12, 0));
    private final JLabel lblResumen = new JLabel(" ");
    private final JLabel lblResumenDetalle = new JLabel(" ");
    private final JPanel contenedorFilas = Estilo.transparente(new BorderLayout());

    private Runnable alModificar = () -> { };
    private Runnable alEliminar = () -> { };
    private Runnable alCambiarSeleccion = () -> { };

    /**
     * @param textoNuevo  texto del botón de alta, por ejemplo "Nueva zona"
     * @param columnas    cuántas columnas tiene la lista
     * @param disposicion filas dentro de tarjetas, o una tarjeta por elemento
     */
    protected PanelAbm(String textoNuevo, int columnas, Disposicion disposicion) {
        super(new BorderLayout(0, 22));
        this.columnas = Math.max(1, columnas);
        this.disposicion = disposicion;
        this.btnNuevo = new Boton(textoNuevo, new Icono(Icono.Tipo.MAS, 16, Color.WHITE), Boton.Tipo.PRIMARIO);
        setOpaque(false);
        setBorder(Estilo.relleno(4, 0, 0, 0));
        btnActualizar.setToolTipText("Actualizar");
        btnActualizar.setBorder(Estilo.relleno(12, 12, 12, 12));
        lblResumen.setFont(Estilo.fuente(Font.BOLD, 16f));
        lblResumen.setForeground(Estilo.TEXTO);
        lblResumenDetalle.setFont(Estilo.fuente(Font.PLAIN, 16f));
        lblResumenDetalle.setForeground(Estilo.TEXTO_SUAVE);
        habilitarAcciones(false);
    }

    /**
     * Arma la pantalla. Lo llama {@link #conectar} (al final del constructor de la subclase),
     * cuando la subclase ya tiene sus campos listos.
     */
    private void armar() {
        JComponent filtros = armarFiltros();
        JPanel derecha = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(botonesExtra);
        derecha.add(btnActualizar);
        derecha.add(Box.createHorizontalStrut(14));
        derecha.add(btnNuevo);

        JPanel encabezado = Estilo.transparente(new BorderLayout(16, 0));
        encabezado.add(filtros != null ? filtros : centradoEnAlto(armarResumen()), BorderLayout.CENTER);
        encabezado.add(derecha, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(contenedorFilas);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel lista = Estilo.transparente(new BorderLayout(0, 16));
        lista.add(scroll, BorderLayout.CENTER);
        JComponent nota = armarNota();
        if (nota != null) {
            lista.add(nota, BorderLayout.SOUTH);
        } else if (filtros != null) {
            lista.add(armarResumen(), BorderLayout.SOUTH); // con filtros arriba, el resumen va abajo
        }

        JComponent detalle = armarDetalle();
        JComponent centro = lista;
        if (detalle != null) {
            lista.setPreferredSize(new Dimension(100, 100));
            JPanel conDetalle = Estilo.transparente(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weighty = 1;
            c.weightx = 1 - pesoDetalle();
            c.insets = new Insets(0, 0, 0, 14);
            conDetalle.add(lista, c);
            c.gridx = 1;
            c.weightx = pesoDetalle();
            c.insets = new Insets(0, 14, 0, 0);
            conDetalle.add(arriba(detalle), c);
            centro = conDetalle;
        }

        add(encabezado, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        dibujarFilas();
        mostrarDetalle(null);
    }

    // ---------- lo que define cada pantalla ----------

    /** Cómo se ve un elemento. En LISTA es el contenido de la fila; en TARJETAS, la tarjeta completa. */
    protected abstract JComponent crearFila(T fila, boolean elegida);

    /** Panel de la derecha con el detalle del elegido; null si la pantalla no tiene. */
    protected JComponent armarDetalle() {
        return null;
    }

    /** Muestra el elemento elegido en el panel de detalle (null = ninguno). */
    protected void mostrarDetalle(T fila) {
    }

    /** Parte del ancho que ocupa el detalle (de 0 a 1). */
    protected double pesoDetalle() {
        return 0.36;
    }

    /** Filtros que van arriba en lugar del resumen (el resumen pasa abajo de la lista); null si no hay. */
    protected JComponent armarFiltros() {
        return null;
    }

    /** Si el elemento pasa los filtros. Después de cambiar un filtro, llamar a {@link #refiltrar()}. */
    protected boolean incluir(T fila) {
        return true;
    }

    /** Orden en que se muestran; null deja el orden en que vienen. */
    protected Comparator<T> orden() {
        return null;
    }

    /** Nota debajo de la lista (por ejemplo, una aclaración); null si no hay. */
    protected JComponent armarNota() {
        return null;
    }

    /** El resumen de arriba: "6 categorías · 6 activas" (lo anterior al primer "·" va en negrita). */
    protected JComponent armarResumen() {
        JPanel resumen = Estilo.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        resumen.add(lblResumen);
        resumen.add(lblResumenDetalle);
        return resumen;
    }

    /** Muestra el resumen que manda el controlador. Una pantalla puede mostrarlo a su manera. */
    protected void mostrarResumen(String resumen) {
        int corte = resumen.indexOf(" · ");
        lblResumen.setText(corte < 0 ? resumen : resumen.substring(0, corte));
        lblResumenDetalle.setText(corte < 0 ? "" : resumen.substring(corte));
    }

    // ---------- ayudas para las pantallas ----------

    /** Elige el elemento y ejecuta la acción (para los botones que van dentro de cada fila o tarjeta). */
    protected void sobre(T fila, Runnable accion) {
        elegir(fila);
        accion.run();
    }

    protected void modificar(T fila) {
        sobre(fila, alModificar);
    }

    protected void eliminar(T fila) {
        sobre(fila, alEliminar);
    }

    /** Vuelve a dibujar la lista con los filtros actuales (y suelta el elegido si quedó afuera). */
    protected void refiltrar() {
        if (elegido != null && !incluir(elegido)) {
            elegir(null);
        }
        dibujarFilas();
    }

    /** Todo lo cargado, sin filtrar. */
    protected List<T> todas() {
        return List.copyOf(filas);
    }

    /** Eliminar y Modificar uno al lado del otro, para el pie del detalle. */
    protected JComponent botonesDetalle() {
        return botonesDetalle(btnModificar);
    }

    /** Eliminar y otra acción principal (por ejemplo "Guardar cambios" cuando se edita en el detalle). */
    protected JComponent botonesDetalle(JButton principal) {
        btnEliminar.setBorder(Estilo.relleno(14, 20, 14, 20));
        principal.setBorder(Estilo.relleno(14, 20, 14, 22));
        principal.setFont(Estilo.fuente(Font.BOLD, 16f));
        JPanel botones = Estilo.transparente(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 10);
        botones.add(btnEliminar, c);
        c.weightx = 1;
        c.insets = new Insets(0, 0, 0, 0);
        botones.add(principal, c);
        return botones;
    }

    /** Un botón chiquito de ícono (lápiz, tacho) para dentro de una fila o tarjeta. */
    protected static JButton botonIcono(Icono.Tipo tipo, Color color, String ayuda) {
        return Boton.soloIcono(tipo, color, ayuda);
    }

    // ---------- conexión con el controlador ----------

    /** Conecta los botones y la selección con el controlador. Llamarlo al final del constructor de la subclase. */
    protected void conectar(Runnable cargar, Runnable nuevo, Runnable modificar, Runnable eliminar,
                            Runnable alCambiarSeleccion) {
        this.alModificar = modificar;
        this.alEliminar = eliminar;
        this.alCambiarSeleccion = alCambiarSeleccion;
        btnActualizar.addActionListener(e -> cargar.run());
        btnNuevo.addActionListener(e -> nuevo.run());
        btnModificar.addActionListener(e -> modificar.run());
        btnEliminar.addActionListener(e -> eliminar.run());
        armar();
    }

    /** Suma un botón propio de la pantalla, arriba a la izquierda de Actualizar. */
    protected void agregarBoton(JButton boton) {
        botonesExtra.add(boton);
    }

    // ---------- lo que usa el controlador ----------

    public void mostrar(List<T> nuevas, String resumen) {
        filas.clear();
        filas.addAll(nuevas);
        if (elegido != null) {
            // el elegido se reemplaza por su versión recién cargada (o se suelta si ya no está)
            T anterior = elegido;
            elegido = null;
            filas.stream().filter(f -> mismo(f, anterior)).findFirst().ifPresent(f -> elegido = f);
        }
        mostrarResumen(resumen);
        dibujarFilas();
        mostrarDetalle(elegido);
    }

    public Optional<T> seleccionado() {
        return Optional.ofNullable(elegido);
    }

    /** Deja elegido el primer elemento que cumpla la condición (por ejemplo, el que se acaba de guardar). */
    public void seleccionar(Predicate<T> cual) {
        filas.stream().filter(cual).findFirst().ifPresent(this::elegir);
    }

    public void habilitarAcciones(boolean hayFila) {
        btnModificar.setEnabled(hayFila);
        btnEliminar.setEnabled(hayFila);
    }

    public JButton getBotonActualizar() { return btnActualizar; }
    public JButton getBotonNuevo() { return btnNuevo; }
    public JButton getBotonModificar() { return btnModificar; }
    public JButton getBotonEliminar() { return btnEliminar; }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public boolean confirmar(String pregunta) {
        return Dialogos.confirmar(this, pregunta);
    }

    // ---------- armado de la lista ----------

    /**
     * Si dos objetos son el mismo elemento (por ejemplo, la misma zona recargada de la base).
     * Por defecto compara con equals; las pantallas cuyos elementos no lo redefinen comparan por id.
     */
    protected boolean mismo(T a, T b) {
        return a.equals(b);
    }

    private void elegir(T fila) {
        elegido = fila;
        dibujarFilas();
        mostrarDetalle(fila);
        alCambiarSeleccion.run();
    }

    private void dibujarFilas() {
        List<T> visibles = new ArrayList<>(filas.stream().filter(this::incluir).toList());
        Comparator<T> comparador = orden();
        if (comparador != null) {
            visibles.sort(comparador);
        }
        JPanel grilla = Estilo.transparente(new GridLayout(0, columnas, 24, 24));
        if (disposicion == Disposicion.TARJETAS) {
            for (T fila : visibles) {
                grilla.add(conClic(crearFila(fila, mismoElegido(fila)), fila));
            }
        } else {
            // las filas se reparten en columnas, cada columna es una tarjeta
            int porColumna = (int) Math.ceil(visibles.size() / (double) columnas);
            for (int col = 0; col < columnas; col++) {
                int desde = Math.min(col * porColumna, visibles.size());
                int hasta = Math.min(desde + porColumna, visibles.size());
                if (desde >= hasta && col > 0) {
                    break;
                }
                grilla.add(columna(visibles.subList(desde, hasta)));
            }
        }
        contenedorFilas.removeAll();
        if (!visibles.isEmpty()) {
            contenedorFilas.add(grilla, BorderLayout.NORTH);
        }
        contenedorFilas.revalidate();
        contenedorFilas.repaint();
    }

    private boolean mismoElegido(T fila) {
        return elegido != null && mismo(fila, elegido);
    }

    /** Una tarjeta con filas separadas por una línea. */
    private JComponent columna(List<T> filasColumna) {
        JPanel filasPanel = Estilo.transparente(null);
        filasPanel.setLayout(new BoxLayout(filasPanel, BoxLayout.Y_AXIS));
        for (int i = 0; i < filasColumna.size(); i++) {
            T fila = filasColumna.get(i);
            Fila envoltorio = new Fila(crearFila(fila, mismoElegido(fila)), mismoElegido(fila), i > 0);
            filasPanel.add(conClic(envoltorio, fila));
        }
        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(filasPanel, BorderLayout.NORTH);
        return tarjeta;
    }

    /** Clic elige el elemento; doble clic lo modifica. Los botones de adentro siguen con su propia acción. */
    private JComponent conClic(JComponent componente, T fila) {
        MouseAdapter clic = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    modificar(fila);
                } else if (!mismoElegido(fila)) {
                    elegir(fila);
                }
            }
        };
        escucharClics(componente, clic);
        componente.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return componente;
    }

    private static void escucharClics(Component c, MouseAdapter clic) {
        if (c instanceof AbstractButton) {
            return; // los botones e interruptores de la fila hacen lo suyo
        }
        c.addMouseListener(clic);
        if (c instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) {
                escucharClics(hijo, clic);
            }
        }
    }

    private static JComponent centradoEnAlto(JComponent c) {
        JPanel p = Estilo.transparente(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.weightx = 1;
        p.add(c, g);
        return p;
    }

    /** Deja un componente arriba, con su alto (el ancho lo reparte la grilla según {@link #pesoDetalle}). */
    private static JComponent arriba(JComponent c) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(100, super.getPreferredSize().height);
            }
        };
        p.setOpaque(false);
        p.add(c, BorderLayout.NORTH);
        return p;
    }

    /** Fila de la lista: crema y con una rayita café a la izquierda cuando está elegida. */
    private static class Fila extends JPanel {
        private final boolean elegida;

        Fila(JComponent contenido, boolean elegida, boolean lineaArriba) {
            super(new BorderLayout());
            this.elegida = elegida;
            setOpaque(false);
            setBorder(BorderFactory.createCompoundBorder(
                    lineaArriba ? BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDE) : Estilo.relleno(0, 0, 0, 0),
                    Estilo.relleno(16, 26, 16, 26)));
            contenido.setOpaque(false);
            add(contenido, BorderLayout.CENTER);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (elegida) {
                Graphics2D g2 = Estilo.suave(g);
                g2.setColor(Estilo.FILA_SELECCIONADA);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Estilo.CAFE);
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        }
    }
}
