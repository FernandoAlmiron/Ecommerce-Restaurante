package org.example.escritorio.vista.rrhh;

import org.example.Modelo.persona.Empleado;
import org.example.Modelo.persona.Sector;
import org.example.escritorio.controlador.rrhh.EmpleadoEdicionControlador;
import org.example.escritorio.vista.comun.Dialogos;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.List;
import java.util.Optional;

/**
 * Alta o modificación de los datos de un empleado (RF-53, RF-54). Con empleado null es un alta.
 * En el alta se cargan también la clave y el sector; al modificar no, porque tienen sus propias acciones.
 */
public class EmpleadoDialog extends JDialog {

    private final Empleado empleado;

    private final JTextField txtNombre = new JTextField(22);
    private final JTextField txtApellido = new JTextField(22);
    private final JTextField txtDni = new JTextField(10);
    private final JTextField txtCelular = new JTextField(14);
    private final JTextField txtUsuario = new JTextField(16);
    private final JPasswordField txtClave = new JPasswordField(16);
    private final JPasswordField txtRepetirClave = new JPasswordField(16);
    private final JComboBox<Sector> cmbSector = new JComboBox<>();
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnCancelar = new JButton("Cancelar");

    private final EmpleadoEdicionControlador controlador;
    private Empleado guardado;

    public EmpleadoDialog(Window padre, Empleado empleado) {
        super(padre, empleado == null ? "Nuevo empleado" : "Modificar empleado", ModalityType.APPLICATION_MODAL);
        this.empleado = empleado;
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

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        raiz.add(armarFormulario(), BorderLayout.CENTER);
        raiz.add(armarBotones(), BorderLayout.SOUTH);
        setContentPane(raiz);

        if (empleado != null) {
            txtNombre.setText(empleado.getNombre());
            txtApellido.setText(empleado.getApellido());
            txtDni.setText(empleado.getDni() == 0 ? "" : String.valueOf(empleado.getDni()));
            txtCelular.setText(empleado.getCelular() == null ? "" : empleado.getCelular());
            txtUsuario.setText(empleado.getUsername());
        }

        controlador = new EmpleadoEdicionControlador(this);
        btnGuardar.addActionListener(e -> controlador.guardar());
        btnCancelar.addActionListener(e -> dispose());
        if (empleado == null) {
            controlador.cargarSectores();
        }

        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    private JPanel armarFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 12);

        JLabel titulo = new JLabel(empleado == null ? "Nuevo empleado"
                : empleado.getApellido() + ", " + empleado.getNombre() + " · legajo " + empleado.getLegajo());
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(titulo, c);
        c.gridwidth = 1;

        int y = 1;
        fila(panel, c, y++, "Nombre", txtNombre);
        fila(panel, c, y++, "Apellido", txtApellido);
        fila(panel, c, y++, "DNI", txtDni);
        fila(panel, c, y++, "Celular", txtCelular);
        fila(panel, c, y++, "Usuario", txtUsuario);
        if (empleado == null) {
            fila(panel, c, y++, "Clave", txtClave);
            fila(panel, c, y++, "Repetir clave", txtRepetirClave);
            fila(panel, c, y, "Sector", cmbSector);
        }
        return panel;
    }

    private void fila(JPanel panel, GridBagConstraints c, int y, String etiqueta, JComponent campo) {
        c.gridx = 0; c.gridy = y; c.weightx = 0;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1; c.weightx = 1;
        panel.add(campo, c);
    }

    private JPanel armarBotones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnCancelar);
        botones.add(btnGuardar);
        getRootPane().setDefaultButton(btnGuardar);
        return botones;
    }

    // ---------- lo que usa el controlador ----------

    /** null si es un alta. */
    public Empleado getEmpleado() { return empleado; }
    public String getNombre() { return txtNombre.getText().trim(); }
    public String getApellido() { return txtApellido.getText().trim(); }
    public String getDniTexto() { return txtDni.getText().trim(); }
    public String getCelular() { return txtCelular.getText().trim(); }
    public String getUsuario() { return txtUsuario.getText().trim(); }
    public char[] getClave() { return txtClave.getPassword(); }
    public char[] getRepetirClave() { return txtRepetirClave.getPassword(); }
    public Sector getSector() { return (Sector) cmbSector.getSelectedItem(); }

    public JButton getBotonGuardar() { return btnGuardar; }
    public JComboBox<Sector> getComboSector() { return cmbSector; }

    public void mostrarSectores(List<Sector> sectores) {
        cmbSector.removeAllItems();
        sectores.forEach(cmbSector::addItem);
    }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public void cerrarCon(Empleado empleadoGuardado) {
        guardado = empleadoGuardado;
        dispose();
    }

    public Optional<Empleado> getGuardado() {
        return Optional.ofNullable(guardado);
    }
}