package org.example.escritorio.vista;

import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.admin.AdministracionPanel;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Icono;
import org.example.escritorio.vista.comun.PanelPendiente;
import org.example.escritorio.vista.recepcion.ReservasPanel;
import org.example.escritorio.vista.salon.TicketsPanel;
import org.example.escritorio.vista.cocina.CocinaPanel;
import org.example.escritorio.vista.caja.CajaPanel;
import org.example.escritorio.vista.reparto.RepartoPanel;
import org.example.escritorio.vista.rrhh.EmpleadosPanel;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Ventana principal: barra lateral café con el logo, las áreas (solo las de su rol; ADMIN ve todas)
 * y el empleado logueado abajo; en el centro, el panel del área elegida.
 */
public class PrincipalFrame extends JFrame {

    private static final int ANCHO_MENU = 260;

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final Map<Area, JComponent> panelesCreados = new EnumMap<>(Area.class);

    public PrincipalFrame() {
        super("La Nona - " + Sesion.rol().descripcion());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        List<Area> areas = Area.permitidasPara(Sesion.rol());

        contenido.setBackground(Estilo.FONDO);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FONDO);
        raiz.add(armarMenu(areas), BorderLayout.WEST);
        raiz.add(contenido, BorderLayout.CENTER);
        setContentPane(raiz);

        if (!areas.isEmpty()) {
            mostrar(areas.get(0));
        }

        setSize(1360, 840);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // se abre maximizada despues del login
    }

    /**
     * Crea el panel de cada área. En cada fase se reemplaza el PanelPendiente por la pantalla real.
     */
    protected JComponent crearPanel(Area area) {
        return switch (area) {
            case RECEPCION -> new ReservasPanel();
            case SALON -> new TicketsPanel();
            case COCINA -> new CocinaPanel();
            case CAJA -> new CajaPanel();
            case REPARTO -> new RepartoPanel();
            case RRHH -> new EmpleadosPanel();
            case ADMINISTRACION -> new AdministracionPanel();
        };
    }

    /** Los paneles se crean la primera vez que se eligen, así no se piden datos de áreas que no se usan. */
    private void mostrar(Area area) {
        if (!panelesCreados.containsKey(area)) {
            JComponent panel = crearPanel(area);
            panelesCreados.put(area, panel);
            contenido.add(panel, area.name());
        }
        tarjetas.show(contenido, area.name());
    }

    // ---------- barra lateral ----------

    private JPanel armarMenu(List<Area> areas) {
        JPanel menu = new JPanel(new BorderLayout());
        menu.setBackground(Estilo.CAFE);
        menu.setBorder(Estilo.relleno(36, 18, 28, 18));
        menu.setPreferredSize(new Dimension(ANCHO_MENU, 0));

        JPanel arriba = Estilo.transparente(null);
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        arriba.add(armarLogo());
        arriba.add(Box.createVerticalStrut(40));

        ButtonGroup grupo = new ButtonGroup();
        boolean primero = true;
        for (Area area : areas) {
            ItemMenu item = new ItemMenu(area.titulo(), new Icono(iconoDe(area), 20));
            item.addActionListener(e -> mostrar(area));
            grupo.add(item);
            arriba.add(item);
            arriba.add(Box.createVerticalStrut(6));
            if (primero) {
                item.setSelected(true);
                primero = false;
            }
        }

        menu.add(arriba, BorderLayout.NORTH);
        menu.add(armarUsuario(), BorderLayout.SOUTH);
        return menu;
    }

    private static Icono.Tipo iconoDe(Area area) {
        return switch (area) {
            case RECEPCION -> Icono.Tipo.CALENDARIO;
            case SALON -> Icono.Tipo.MAS_CIRCULO;
            case COCINA -> Icono.Tipo.OLLA;
            case CAJA -> Icono.Tipo.BILLETE;
            case REPARTO -> Icono.Tipo.BICI;
            case RRHH -> Icono.Tipo.PERSONAS;
            case ADMINISTRACION -> Icono.Tipo.ENGRANAJE;
        };
    }

    /** "La Nona" en cursiva, "pastas caseras" y la rayita decorativa. */
    private JComponent armarLogo() {
        JLabel nombre = new JLabel("La Nona", SwingConstants.CENTER);
        nombre.setFont(Estilo.fuenteLogo(40f));
        nombre.setForeground(Color.WHITE);
        nombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel bajada = new JLabel("pastas caseras", SwingConstants.CENTER);
        bajada.setFont(Estilo.fuenteSerif(17f));
        bajada.setForeground(new Color(255, 255, 255, 225));
        bajada.setAlignmentX(Component.CENTER_ALIGNMENT);

        JComponent raya = new Estilo.Rodillo(116, 76, 10);

        JPanel logo = Estilo.transparente(null);
        logo.setLayout(new BoxLayout(logo, BoxLayout.Y_AXIS));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.add(nombre);
        logo.add(bajada);
        logo.add(Box.createVerticalStrut(8));
        logo.add(raya);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, logo.getPreferredSize().height));
        return logo;
    }

    /** Tarjeta de abajo: iniciales, nombre, rol y el botón para cerrar sesión. */
    private JComponent armarUsuario() {
        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new GridBagLayout(), 18) {
            @Override
            protected void paintBorder(Graphics g) {
                // sin borde: solo el fondo más oscuro
            }
        };
        tarjeta.setBackground(Estilo.CAFE_OSCURO);
        tarjeta.setBorder(Estilo.relleno(14, 16, 14, 12));

        JComponent avatar = new Avatar(iniciales(Sesion.nombreCompleto()));

        JLabel nombre = new JLabel("<html>" + escapar(Sesion.nombreCompleto()) + "</html>");
        nombre.setFont(Estilo.fuente(Font.BOLD, 15f));
        nombre.setForeground(Color.WHITE);

        JLabel rol = new JLabel(Sesion.rol().descripcion());
        rol.setFont(Estilo.fuente(Font.PLAIN, 14f));
        rol.setForeground(new Color(255, 255, 255, 210));

        JPanel textos = Estilo.transparente(null);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(nombre);
        textos.add(rol);

        JButton salir = new JButton(new Icono(Icono.Tipo.SALIR, 20, Color.WHITE));
        salir.setContentAreaFilled(false);
        salir.setBorderPainted(false);
        salir.setFocusPainted(false);
        salir.setOpaque(false);
        salir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        salir.setToolTipText("Cerrar sesión");
        salir.putClientProperty("JButton.buttonType", "borderless");
        salir.addActionListener(e -> cerrarSesion());

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 12);
        tarjeta.add(avatar, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 4);
        tarjeta.add(textos, c);
        c.gridx = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, 0, 0);
        tarjeta.add(salir, c);
        return tarjeta;
    }

    private static String iniciales(String nombreCompleto) {
        StringBuilder sb = new StringBuilder();
        for (String parte : nombreCompleto.trim().split("\\s+")) {
            if (!parte.isEmpty() && sb.length() < 2) {
                sb.append(Character.toUpperCase(parte.charAt(0)));
            }
        }
        return sb.toString();
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void cerrarSesion() {
        if (Dialogos.confirmar(this, "¿Querés cerrar la sesión?")) {
            Sesion.cerrar();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    /** Opción del menú lateral: blanca con texto café cuando está elegida, transparente si no. */
    private static class ItemMenu extends JToggleButton {
        ItemMenu(String texto, Icono icono) {
            super(texto, icono);
            setHorizontalAlignment(SwingConstants.LEFT);
            setIconTextGap(16);
            setFont(Estilo.fuente(Font.PLAIN, 17f));
            setBorder(Estilo.relleno(13, 18, 13, 18));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            putClientProperty("JButton.buttonType", "borderless");
            getModel().addChangeListener(e -> actualizarColores());
            actualizarColores();
        }

        private void actualizarColores() {
            setForeground(isSelected() ? Estilo.CAFE : Color.WHITE);
            setFont(Estilo.fuente(isSelected() ? Font.BOLD : Font.PLAIN, 17f));
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Estilo.suave(g);
            if (isSelected()) {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(255, 255, 255, 28));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Círculo blanco con las iniciales del empleado. */
    private static class Avatar extends JComponent {
        private final String iniciales;

        Avatar(String iniciales) {
            this.iniciales = iniciales;
            Dimension tam = new Dimension(50, 50);
            setPreferredSize(tam);
            setMinimumSize(tam);
            setFont(Estilo.fuente(Font.BOLD, 17f));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Estilo.suave(g);
            g2.setColor(Color.WHITE);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(Estilo.CAFE_OSCURO);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(iniciales)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(iniciales, x, y);
            g2.dispose();
        }
    }
}
