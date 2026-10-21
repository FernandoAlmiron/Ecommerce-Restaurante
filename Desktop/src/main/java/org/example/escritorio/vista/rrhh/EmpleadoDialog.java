package org.example.escritorio.vista.rrhh;

import org.example.Modelo.persona.Empleado;
import org.example.Modelo.persona.Sector;
import org.example.escritorio.controlador.rrhh.EmpleadoEdicionControlador;
import org.example.escritorio.vista.comun.DialogoFormulario;
import org.example.escritorio.vista.comun.Formulario;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Alta o modificación de los datos de un empleado (RF-53, RF-54). Con empleado null es un alta.
 * En el alta se cargan también la clave y el sector; al modificar no, porque tienen sus propias acciones.
 */
public class EmpleadoDialog extends DialogoFormulario {

    private final Empleado empleado;

    private final JTextField txtNombre = new JTextField(22);
    private final JTextField txtApellido = new JTextField(22);
    private final JTextField txtDni = new JTextField(10);
    private final JTextField txtCelular = new JTextField(14);
    private final JTextField txtUsuario = new JTextField(16);
    private final JPasswordField txtClave = new JPasswordField(16);
    private final JPasswordField txtRepetirClave = new JPasswordField(16);
    private final JComboBox<Sector> cmbSector = new JComboBox<>();

    private final EmpleadoEdicionControlador controlador;
    private Empleado guardado;

    public EmpleadoDialog(Window padre, Empleado empleado) {
        super(padre, empleado == null ? "Recursos humanos" : "Recursos humanos · Legajo " + empleado.getLegajo(),
                empleado == null ? "Nuevo empleado" : "Modificar datos",
                empleado == null ? "Completá sus datos, el usuario para entrar al sistema y el sector."
                        : empleado.getApellido() + ", " + empleado.getNombre(),
                "Guardar");
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

        if (empleado != null) {
            txtNombre.setText(empleado.getNombre());
            txtApellido.setText(empleado.getApellido());
            txtDni.setText(empleado.getDni() == 0 ? "" : String.valueOf(empleado.getDni()));
            txtCelular.setText(empleado.getCelular() == null ? "" : empleado.getCelular());
            txtUsuario.setText(empleado.getUsername());
        }

        controlador = new EmpleadoEdicionControlador(this);
        btnPrincipal.addActionListener(e -> controlador.guardar());
        if (empleado == null) {
            controlador.cargarSectores();
        }

        armar();
    }

    @Override
    protected JComponent armarCuerpo() {
        Formulario.estilizar(txtNombre, null);
        Formulario.estilizar(txtApellido, null);
        Formulario.estilizar(txtDni, "Sin puntos");
        Formulario.estilizar(txtCelular, "Opcional");
        Formulario.estilizar(txtUsuario, "Con este entra al sistema");
        Formulario.estilizar(txtClave, null);
        Formulario.estilizar(txtRepetirClave, null);
        Formulario.estilizar(cmbSector);

        List<JComponent> filas = new ArrayList<>(List.of(
                Formulario.enFila(
                        Formulario.campo("Nombre", txtNombre, null),
                        Formulario.campo("Apellido", txtApellido, null)),
                Formulario.enFila(
                        Formulario.campo("DNI", txtDni, null),
                        Formulario.campo("Celular", txtCelular, null)),
                Formulario.campo("Usuario", txtUsuario, null)));
        if (empleado == null) {
            // la clave y el sector solo se cargan en el alta; después tienen sus propias acciones
            filas.add(Formulario.enFila(
                    Formulario.campo("Clave", txtClave, null),
                    Formulario.campo("Repetir clave", txtRepetirClave, null)));
            filas.add(Formulario.campo("Sector", cmbSector, null));
        }
        return Formulario.cuerpo(filas.toArray(new JComponent[0]));
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

    public JButton getBotonGuardar() { return btnPrincipal; }
    public JComboBox<Sector> getComboSector() { return cmbSector; }

    public void mostrarSectores(List<Sector> sectores) {
        cmbSector.removeAllItems();
        sectores.forEach(cmbSector::addItem);
    }

    public void cerrarCon(Empleado empleadoGuardado) {
        guardado = empleadoGuardado;
        dispose();
    }

    public Optional<Empleado> getGuardado() {
        return Optional.ofNullable(guardado);
    }
}