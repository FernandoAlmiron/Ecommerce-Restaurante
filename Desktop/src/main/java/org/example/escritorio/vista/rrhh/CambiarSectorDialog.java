package org.example.escritorio.vista.rrhh;

import org.example.Modelo.persona.Empleado;
import org.example.Modelo.persona.Sector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.List;
import java.util.Optional;

/**
 * Elige el nuevo sector de un empleado (RF-54). Solo elige: el cambio lo hace el controlador.
 * Los sectores que se ofrecen los decide el controlador (ADMINISTRACION solo para ADMIN).
 */
public final class CambiarSectorDialog extends JDialog {

    private final JComboBox<Sector> cmbSector = new JComboBox<>();
    private Sector elegido;

    private CambiarSectorDialog(Window padre, Empleado empleado, List<Sector> sectores) {
        super(padre, "Cambiar sector", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        cmbSector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof Sector s) {
                    setText(s.getNombre());
                }
                return this;
            }
        });
        sectores.forEach(cmbSector::addItem);

        JLabel titulo = new JLabel(empleado.getApellido() + ", " + empleado.getNombre());
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 15f));

        JPanel datos = new JPanel(new GridLayout(0, 2, 12, 6));
        datos.add(new JLabel("Sector actual"));
        datos.add(new JLabel(EmpleadoTableModel.sector(empleado)));
        datos.add(new JLabel("Nuevo sector"));
        datos.add(cmbSector);

        JButton btnCambiar = new JButton("Cambiar");
        JButton btnCancelar = new JButton("Cancelar");
        btnCambiar.addActionListener(e -> {
            elegido = (Sector) cmbSector.getSelectedItem();
            dispose();
        });
        btnCancelar.addActionListener(e -> dispose());
        btnCambiar.setEnabled(!sectores.isEmpty());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnCancelar);
        botones.add(btnCambiar);
        getRootPane().setDefaultButton(btnCambiar);

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        raiz.add(titulo, BorderLayout.NORTH);
        raiz.add(datos, BorderLayout.CENTER);
        raiz.add(botones, BorderLayout.SOUTH);
        setContentPane(raiz);

        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    /** Abre la ventana y devuelve el sector elegido, o vacío si se canceló. */
    public static Optional<Sector> pedirSector(Component padre, Empleado empleado, List<Sector> sectores) {
        Window ventana = padre instanceof Window w ? w : SwingUtilities.getWindowAncestor(padre);
        CambiarSectorDialog dialogo = new CambiarSectorDialog(ventana, empleado, sectores);
        dialogo.setVisible(true);
        return Optional.ofNullable(dialogo.elegido);
    }
}