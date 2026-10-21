package org.example.escritorio.vista.admin;

import org.example.Modelo.reserva.Zona;
import org.example.escritorio.controlador.admin.ZonasControlador;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Icono;
import org.example.escritorio.vista.comun.Interruptor;
import org.example.escritorio.vista.comun.PanelAbm;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Comparator;

/**
 * Solapa Zonas de Administración (RF-51): las zonas del salón y su capacidad.
 * Cada zona es una tarjeta con su capacidad, la parte que representa del total y un interruptor para habilitarla.
 */
public class ZonasPanel extends PanelAbm<Zona> {

    private final ZonasControlador controlador;
    private final JLabel lblCapacidad = new JLabel("0");
    private final JLabel lblCapacidadTexto = new JLabel(" ");

    public ZonasPanel() {
        super("Nueva zona", 2, Disposicion.TARJETAS);
        controlador = new ZonasControlador(this);
        conectar(() -> controlador.cargar(null), controlador::nuevo, controlador::modificar,
                controlador::eliminar, controlador::alCambiarSeleccion);
        controlador.cargar(null);
    }

    @Override
    protected Comparator<Zona> orden() {
        // las habilitadas primero, de la más grande a la más chica
        return Comparator.comparing((Zona z) -> !z.isActiva())
                .thenComparing(Comparator.comparingInt(Zona::getCapacidadMaxima).reversed());
    }

    @Override
    protected boolean mismo(Zona a, Zona b) {
        return a.getIdZona() == b.getIdZona();
    }

    /** "72 personas de capacidad activa · 3 de 4 zonas habilitadas", con el número grande. */
    @Override
    protected JComponent armarResumen() {
        lblCapacidad.setFont(Estilo.fuente(Font.BOLD, 34f));
        lblCapacidad.setForeground(Estilo.CAFE_OSCURO);
        lblCapacidad.setBorder(Estilo.relleno(0, 0, 0, 12));
        lblCapacidadTexto.setFont(Estilo.fuente(Font.PLAIN, 17f));
        lblCapacidadTexto.setForeground(Estilo.TEXTO_SUAVE);
        lblCapacidadTexto.setBorder(Estilo.relleno(0, 0, 0, 14)); // aire para que no se corte la última letra
        // el texto va en el centro para que use todo el ancho que quede y no se corte
        JPanel resumen = Estilo.transparente(new BorderLayout());
        resumen.add(lblCapacidad, BorderLayout.WEST);
        resumen.add(lblCapacidadTexto, BorderLayout.CENTER);
        return resumen;
    }

    @Override
    protected void mostrarResumen(String resumen) {
        int capacidad = capacidadActiva();
        long activas = todas().stream().filter(Zona::isActiva).count();
        lblCapacidad.setText(String.valueOf(capacidad));
        lblCapacidadTexto.setText("personas de capacidad activa · " + activas + " de " + todas().size()
                + (todas().size() == 1 ? " zona habilitada" : " zonas habilitadas"));
    }

    private int capacidadActiva() {
        return todas().stream().filter(Zona::isActiva).mapToInt(Zona::getCapacidadMaxima).sum();
    }

    @Override
    protected JComponent crearFila(Zona zona, boolean elegida) {
        boolean activa = zona.isActiva();

        JLabel nombre = new JLabel(zona.getNombre());
        nombre.setFont(Estilo.fuente(Font.BOLD, 22f));
        nombre.setForeground(activa ? Estilo.TEXTO : Estilo.TEXTO_SUAVE);
        nombre.setBorder(Estilo.relleno(0, 0, 0, 6));
        JLabel estado = new JLabel(activa ? "Activa" : "Inactiva · no recibe reservas");
        estado.setFont(Estilo.fuente(Font.BOLD, 15f));
        estado.setForeground(activa ? Estilo.VERDE_TEXTO : Estilo.TEXTO_SUAVE);
        JPanel textos = Estilo.transparente(null);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(nombre);
        textos.add(Box.createVerticalStrut(2));
        textos.add(estado);

        Interruptor interruptor = new Interruptor(activa);
        interruptor.setToolTipText(activa ? "Deshabilitar la zona" : "Habilitar la zona");
        interruptor.addActionListener(e -> sobre(zona, controlador::activarODesactivar));
        JPanel arriba = Estilo.transparente(new BorderLayout());
        arriba.add(textos, BorderLayout.CENTER);
        arriba.add(interruptor, BorderLayout.EAST);

        // capacidad grande y la parte del total
        JLabel numero = new JLabel(String.valueOf(zona.getCapacidadMaxima()));
        numero.setFont(Estilo.fuente(Font.BOLD, 38f));
        numero.setForeground(activa ? Estilo.TEXTO : Estilo.TEXTO_SUAVE);
        numero.setBorder(Estilo.relleno(0, 0, 0, 10));
        JLabel personas = new JLabel("personas");
        personas.setFont(Estilo.fuente(Font.PLAIN, 17f));
        personas.setForeground(Estilo.TEXTO_SUAVE);
        personas.setBorder(Estilo.relleno(0, 0, 0, 4)); // aire para que no se corte la última letra
        JPanel cantidad = Estilo.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cantidad.add(numero);
        cantidad.add(personas);

        int total = capacidadActiva();
        double parte = activa && total > 0 ? zona.getCapacidadMaxima() / (double) total : 0;
        JLabel porcentaje = new JLabel(activa ? Math.round(parte * 100) + "% del total" : "No suma a la capacidad");
        porcentaje.setFont(Estilo.fuente(Font.PLAIN, 16f));
        porcentaje.setForeground(Estilo.TEXTO_SUAVE);
        JPanel filaCantidad = Estilo.transparente(new BorderLayout());
        filaCantidad.add(cantidad, BorderLayout.WEST);
        filaCantidad.add(porcentaje, BorderLayout.EAST);

        JPanel botones = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        var editar = botonIcono(Icono.Tipo.LAPIZ, Estilo.TEXTO, "Modificar la zona");
        editar.addActionListener(e -> modificar(zona));
        var borrar = botonIcono(Icono.Tipo.TACHO, Estilo.ROJO_TEXTO, "Eliminar la zona");
        borrar.addActionListener(e -> eliminar(zona));
        botones.add(editar);
        botones.add(borrar);

        Barra barra = new Barra(parte);
        JPanel cuerpo = Estilo.transparente(null);
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[]{arriba, filaCantidad, barra, botones}) {
            c.setAlignmentX(LEFT_ALIGNMENT);
        }
        cuerpo.add(arriba);
        cuerpo.add(Box.createVerticalStrut(18));
        cuerpo.add(filaCantidad);
        cuerpo.add(Box.createVerticalStrut(10));
        cuerpo.add(barra);
        cuerpo.add(Box.createVerticalStrut(16));
        cuerpo.add(botones);

        TarjetaZona tarjeta = new TarjetaZona(activa, elegida);
        tarjeta.add(cuerpo, BorderLayout.CENTER);
        return tarjeta;
    }

    /** Barra con la parte de la capacidad total que aporta la zona. */
    private static class Barra extends JComponent {
        private final double parte;

        Barra(double parte) {
            this.parte = parte;
            setPreferredSize(new Dimension(10, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Estilo.suave(g);
            int alto = getHeight();
            g2.setColor(Estilo.SEGMENTO_FONDO);
            g2.fillRoundRect(0, 0, getWidth(), alto, alto, alto);
            int lleno = (int) Math.round(getWidth() * parte);
            if (lleno > 0) {
                g2.setColor(Estilo.CAFE);
                g2.fillRoundRect(0, 0, Math.max(lleno, alto), alto, alto, alto);
            }
            g2.dispose();
        }
    }

    /** Tarjeta de la zona: blanca; si está deshabilitada, gris con borde punteado; si está elegida, borde café. */
    private static class TarjetaZona extends Estilo.Tarjeta {
        private final boolean activa;
        private final boolean elegida;

        TarjetaZona(boolean activa, boolean elegida) {
            super(new BorderLayout(), 22);
            this.activa = activa;
            this.elegida = elegida;
            setBorder(Estilo.relleno(24, 28, 22, 28));
            setBackground(activa ? Estilo.BLANCO : new Color(0xFAF8F6));
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = Estilo.suave(g);
            if (elegida) {
                g2.setColor(Estilo.CAFE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 22, 22);
            } else if (!activa) {
                g2.setColor(new Color(0xD9CBBE));
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
                        new float[]{6f, 5f}, 0f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 22, 22);
            } else {
                g2.setColor(Estilo.BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            }
            g2.dispose();
        }
    }
}
