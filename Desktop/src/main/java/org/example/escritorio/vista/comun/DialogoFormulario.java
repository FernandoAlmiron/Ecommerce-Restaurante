package org.example.escritorio.vista.comun;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.Locale;

/**
 * Base de todas las ventanas de carga de datos (altas, modificaciones, walk-in, abrir mesa...)
 * con la estética "La Nona": encabezado con sección y título, el formulario en el medio
 * y abajo Cancelar + la acción principal.
 *
 * Cada ventana concreta solo arma sus campos en {@link #armarCuerpo()} y, al final de su constructor
 * (cuando sus campos ya existen), llama a {@link #armar()}:
 *
 *   public MenuDialog(Window padre, Menu plato) {
 *       super(padre, "Cocina · Menú", "Nuevo plato", "Completá los datos", "Guardar");
 *       ...
 *       armar();
 *   }
 *
 *   protected JComponent armarCuerpo() {
 *       return Formulario.cuerpo(Formulario.campo("Nombre", txtNombre, null), ...);
 *   }
 */
public abstract class DialogoFormulario extends JDialog {

    public static final int ANCHO_MINIMO = 580;

    /**
     * La acción principal (Guardar, Registrar...): café, a la derecha de todo y se activa con Enter.
     * Cancelar cierra la ventana sin hacer nada; si en una ventana se llama distinto ("Listo", "Cerrar"),
     * la subclase le cambia el texto.
     */
    protected final JButton btnPrincipal;
    protected final JButton btnCancelar = new Boton("Cancelar", Boton.Tipo.CONTORNO);

    private final String seccion;
    private final String titulo;
    private final JLabel lblSubtitulo = new JLabel();

    protected DialogoFormulario(Window padre, String seccion, String titulo, String subtitulo, String accionPrincipal) {
        super(padre, titulo, ModalityType.APPLICATION_MODAL);
        this.seccion = seccion;
        this.titulo = titulo;
        this.btnPrincipal = new Boton(accionPrincipal, Boton.Tipo.PRIMARIO);
        setSubtitulo(subtitulo);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        btnCancelar.addActionListener(e -> dispose());
    }

    /** Los campos de la ventana. Se llama una sola vez, desde {@link #armar()}. */
    protected abstract JComponent armarCuerpo();

    /** Ancho mínimo de la ventana; las ventanas chicas pueden pedir menos. */
    protected int anchoMinimo() {
        return ANCHO_MINIMO;
    }

    /** Botones que van a la izquierda de Cancelar (por ejemplo "Buscar"). Por defecto, ninguno. */
    protected JButton[] botonesExtra() {
        return new JButton[0];
    }

    /** Arma la ventana completa. Llamarlo al final del constructor de la subclase. */
    protected final void armar() {
        setContentPane(armarContenido());
        getRootPane().setDefaultButton(btnPrincipal);
        pack();
        java.awt.Rectangle pantalla = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(new Dimension(Math.max(anchoMinimo(), getWidth()), Math.min(getHeight(), pantalla.height - 40)));
        setResizable(false);
        setLocationRelativeTo(getOwner());
    }

    // ---------- lo que pueden usar las subclases y sus controladores ----------

    public JButton getBotonPrincipal() {
        return btnPrincipal;
    }

    /** Texto gris debajo del título; null o vacío lo oculta. */
    public void setSubtitulo(String subtitulo) {
        lblSubtitulo.setText(subtitulo == null ? "" : subtitulo);
        lblSubtitulo.setVisible(subtitulo != null && !subtitulo.isBlank());
    }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    // ---------- armado ----------

    private JPanel armarContenido() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.BLANCO);
        raiz.add(armarCabecera(), BorderLayout.NORTH);

        JComponent cuerpo = armarCuerpo();
        cuerpo.setOpaque(false);
        JPanel conMargen = Estilo.transparente(new BorderLayout());
        conMargen.setBorder(Estilo.relleno(22, 30, 26, 30));
        conMargen.add(cuerpo, BorderLayout.CENTER);
        raiz.add(conMargen, BorderLayout.CENTER);

        raiz.add(armarPie(), BorderLayout.SOUTH);
        return raiz;
    }

    private JComponent armarCabecera() {
        JLabel lblSeccion = new JLabel(seccion.toUpperCase(Locale.forLanguageTag("es-AR")));
        lblSeccion.setFont(Estilo.conEspaciado(Estilo.fuente(Font.BOLD, 13f), 0.1f));
        lblSeccion.setForeground(Estilo.CAFE);
        // las letras separadas ocupan un poco más de lo que mide Swing: margen para no cortar la última
        lblSeccion.setBorder(Estilo.relleno(0, 0, 0, 8));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Estilo.fuente(Font.BOLD, 26f));
        lblTitulo.setForeground(Estilo.TEXTO);

        lblSubtitulo.setFont(Estilo.fuente(Font.PLAIN, 15f));
        lblSubtitulo.setForeground(Estilo.TEXTO_SUAVE);

        JPanel cabecera = Estilo.transparente(null);
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),
                Estilo.relleno(26, 30, 22, 30)));
        if (!seccion.isBlank()) {
            cabecera.add(lblSeccion);
            cabecera.add(Box.createVerticalStrut(6));
        }
        cabecera.add(lblTitulo);
        cabecera.add(Box.createVerticalStrut(4));
        cabecera.add(lblSubtitulo);
        return cabecera;
    }

    private JComponent armarPie() {
        JPanel derecha = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        for (JButton extra : botonesExtra()) {
            derecha.add(extra);
        }
        derecha.add(btnCancelar);
        derecha.add(btnPrincipal);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(Estilo.ENCABEZADO_TABLA);
        pie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDE),
                Estilo.relleno(16, 18, 16, 18)));
        pie.add(derecha, BorderLayout.EAST);
        return pie;
    }
}
