package org.example.escritorio.vista;

import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.PanelPendiente;
import org.example.escritorio.vista.recepcion.ReservasPanel;
import org.example.escritorio.vista.salon.TicketsPanel;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Ventana principal: encabezado con el empleado logueado, menú de áreas a la izquierda
 * (solo las de su rol; ADMIN ve todas) y el panel del área elegida en el centro.
 */
public class PrincipalFrame extends JFrame {

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final Map<Area, JComponent> panelesCreados = new EnumMap<>(Area.class);

    public PrincipalFrame() {
        super("Restaurante - " + Sesion.rol().descripcion());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        List<Area> areas = Area.permitidasPara(Sesion.rol());

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(armarEncabezado(), BorderLayout.NORTH);
        raiz.add(armarMenu(areas), BorderLayout.WEST);
        raiz.add(contenido, BorderLayout.CENTER);
        setContentPane(raiz);

        if (!areas.isEmpty()) {
            mostrar(areas.get(0));
        }

        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    /**
     * Crea el panel de cada área. En cada fase se reemplaza el PanelPendiente por la pantalla real.
     */
    protected JComponent crearPanel(Area area) {
        return switch (area) {
            case RECEPCION -> new ReservasPanel();
            case SALON -> new TicketsPanel();
            case COCINA, CAJA, REPARTO, RRHH, ADMINISTRACION -> new PanelPendiente(area);
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

    private JPanel armarEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new JSeparator().getForeground()),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));

        JLabel empleado = new JLabel(Sesion.nombreCompleto() + "  ·  " + Sesion.rol().descripcion());
        empleado.setFont(empleado.getFont().deriveFont(Font.BOLD));
        encabezado.add(empleado, BorderLayout.WEST);

        JButton btnCerrarSesion = new JButton("Cerrar sesión");
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
        encabezado.add(btnCerrarSesion, BorderLayout.EAST);

        return encabezado;
    }

    private JPanel armarMenu(List<Area> areas) {
        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 6));
        ButtonGroup grupo = new ButtonGroup();
        boolean primero = true;
        for (Area area : areas) {
            JToggleButton boton = new JToggleButton(area.titulo());
            boton.setHorizontalAlignment(JToggleButton.LEFT);
            boton.addActionListener(e -> mostrar(area));
            grupo.add(boton);
            botones.add(boton);
            if (primero) {
                boton.setSelected(true);
                primero = false;
            }
        }

        JPanel menu = new JPanel(new BorderLayout());
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new JSeparator().getForeground()),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        menu.setPreferredSize(new Dimension(200, 0));
        menu.add(botones, BorderLayout.NORTH);
        return menu;
    }

    private void cerrarSesion() {
        if (Dialogos.confirmar(this, "¿Querés cerrar la sesión?")) {
            Sesion.cerrar();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}