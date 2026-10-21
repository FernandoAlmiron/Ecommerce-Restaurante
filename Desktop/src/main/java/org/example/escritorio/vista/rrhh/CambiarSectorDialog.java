package org.example.escritorio.vista.rrhh;

import org.example.Modelo.persona.Empleado;
import org.example.Modelo.persona.Sector;
import org.example.escritorio.vista.comun.DialogoFormulario;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formulario;
import org.example.escritorio.vista.comun.Tablas;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.util.List;
import java.util.Optional;

/**
 * Elige el nuevo sector de un empleado (RF-54). Solo elige: el cambio lo hace el controlador.
 * Los sectores que se ofrecen los decide el controlador (ADMINISTRACION solo para ADMIN).
 */
public final class CambiarSectorDialog extends DialogoFormulario {

    private final Empleado empleado;
    private final JComboBox<Sector> cmbSector = new JComboBox<>();
    private Sector elegido;

    private CambiarSectorDialog(Window padre, Empleado empleado, List<Sector> sectores) {
        super(padre, "Recursos humanos · Legajo " + empleado.getLegajo(), "Cambiar sector",
                empleado.getApellido() + ", " + empleado.getNombre(), "Cambiar");
        this.empleado = empleado;

        cmbSector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof Sector s) {
                    setText(EmpleadoTableModel.textoSector(s.getNombre()));
                }
                return this;
            }
        });
        sectores.forEach(cmbSector::addItem);

        btnPrincipal.addActionListener(e -> {
            elegido = (Sector) cmbSector.getSelectedItem();
            dispose();
        });
        btnPrincipal.setEnabled(!sectores.isEmpty());

        armar();
    }

    @Override
    protected JComponent armarCuerpo() {
        Formulario.estilizar(cmbSector);
        Tablas.Insignia actual = EmpleadoTableModel.insigniaSector(EmpleadoTableModel.sector(empleado));
        Estilo.Pildora lblActual = new Estilo.Pildora(actual.texto(), actual.fondo(), actual.color(),
                Estilo.fuente(Font.BOLD, 14f), -1);
        lblActual.setBorder(Estilo.relleno(5, 14, 5, 14));
        JPanel filaActual = Estilo.transparente(new BorderLayout());
        filaActual.add(lblActual, BorderLayout.WEST);
        return Formulario.cuerpo(
                Formulario.campo("Sector actual", filaActual, null),
                Formulario.campo("Nuevo sector", cmbSector,
                        "Las pantallas nuevas las ve la próxima vez que inicie sesión."));
    }

    /** Abre la ventana y devuelve el sector elegido, o vacío si se canceló. */
    public static Optional<Sector> pedirSector(Component padre, Empleado empleado, List<Sector> sectores) {
        Window ventana = padre instanceof Window w ? w : SwingUtilities.getWindowAncestor(padre);
        CambiarSectorDialog dialogo = new CambiarSectorDialog(ventana, empleado, sectores);
        dialogo.setVisible(true);
        return Optional.ofNullable(dialogo.elegido);
    }
}
