package org.example.escritorio.vista.recepcion;

import org.example.Modelo.persona.Cliente;
import org.example.Modelo.reserva.Zona;
import org.example.dto.ReservaResponseDTO;
import org.example.escritorio.controlador.recepcion.WalkInControlador;
import org.example.escritorio.vista.comun.Dialogos;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
 * Ventana para ubicar a un cliente que llega sin reserva (RF-17).
 * Primero se busca por DNI: si existe se usan sus datos; si no, se completan nombre y apellido.
 */
public class WalkInDialog extends JDialog {

    private final JTextField txtDni = new JTextField(10);
    private final JButton btnBuscar = new JButton("Buscar");
    private final JLabel lblCliente = new JLabel("Ingresá el DNI y tocá Buscar.");
    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtApellido = new JTextField(20);
    private final JTextField txtCelular = new JTextField(20);
    private final JComboBox<Zona> cmbZona = new JComboBox<>();
    private final JSpinner spnComensales = new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));
    private final JButton btnRegistrar = new JButton("Registrar walk-in");
    private final JButton btnCancelar = new JButton("Cancelar");

    private final WalkInControlador controlador;
    private ReservaResponseDTO reservaCreada;

    public WalkInDialog(Window padre) {
        super(padre, "Nuevo walk-in", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        raiz.add(armarFormulario(), BorderLayout.CENTER);
        raiz.add(armarBotones(), BorderLayout.SOUTH);
        setContentPane(raiz);

        cmbZona.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof Zona z) {
                    setText(z.getNombre() + " (hasta " + z.getCapacidadMaxima() + ")");
                }
                return this;
            }
        });

        controlador = new WalkInControlador(this);

        btnBuscar.addActionListener(e -> controlador.buscarCliente());
        txtDni.addActionListener(e -> controlador.buscarCliente()); // Enter en el DNI = Buscar
        txtDni.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { controlador.alCambiarDni(); }
            @Override public void removeUpdate(DocumentEvent e) { controlador.alCambiarDni(); }
            @Override public void changedUpdate(DocumentEvent e) { controlador.alCambiarDni(); }
        });
        btnRegistrar.addActionListener(e -> controlador.registrar());
        btnCancelar.addActionListener(e -> dispose());

        limpiarCliente();
        controlador.cargarZonas();

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

        JLabel titulo = new JLabel("Cliente sin reserva");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(titulo, c);
        c.gridwidth = 1;

        JPanel filaDni = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaDni.add(txtDni);
        filaDni.add(Box.createHorizontalStrut(8));
        filaDni.add(btnBuscar);
        fila(panel, c, 1, "DNI", filaDni);

        lblCliente.setFont(lblCliente.getFont().deriveFont(Font.ITALIC));
        c.gridx = 1; c.gridy = 2;
        panel.add(lblCliente, c);

        fila(panel, c, 3, "Nombre", txtNombre);
        fila(panel, c, 4, "Apellido", txtApellido);
        fila(panel, c, 5, "Celular", txtCelular);
        fila(panel, c, 6, "Zona", cmbZona);
        fila(panel, c, 7, "Comensales", spnComensales);
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
        botones.add(btnRegistrar);
        getRootPane().setDefaultButton(btnRegistrar);
        return botones;
    }

    // ---------- lo que usa el controlador ----------

    public String getDniTexto() { return txtDni.getText().trim(); }
    public String getNombre() { return txtNombre.getText().trim(); }
    public String getApellido() { return txtApellido.getText().trim(); }
    public String getCelular() { return txtCelular.getText().trim(); }
    public int getComensales() { return (Integer) spnComensales.getValue(); }
    public Zona getZonaSeleccionada() { return (Zona) cmbZona.getSelectedItem(); }

    public JButton getBotonBuscar() { return btnBuscar; }
    public JButton getBotonRegistrar() { return btnRegistrar; }
    public JComboBox<Zona> getComboZona() { return cmbZona; }

    public void mostrarZonas(List<Zona> zonas) {
        cmbZona.removeAllItems();
        zonas.forEach(cmbZona::addItem);
    }

    public void mostrarClienteExistente(Cliente cliente) {
        lblCliente.setText("Cliente registrado.");
        txtNombre.setText(cliente.getNombre());
        txtApellido.setText(cliente.getApellido());
        txtCelular.setText(cliente.getCelular() == null ? "" : cliente.getCelular());
        habilitarDatosCliente(false);
        cmbZona.requestFocusInWindow();
    }

    public void prepararClienteNuevo() {
        lblCliente.setText("Cliente nuevo: cargá sus datos.");
        txtNombre.setText("");
        txtApellido.setText("");
        txtCelular.setText("");
        habilitarDatosCliente(true);
        txtNombre.requestFocusInWindow();
    }

    public void limpiarCliente() {
        lblCliente.setText("Ingresá el DNI y tocá Buscar.");
        txtNombre.setText("");
        txtApellido.setText("");
        txtCelular.setText("");
        habilitarDatosCliente(false);
        btnRegistrar.setEnabled(false);
    }

    public void habilitarRegistrar(boolean habilitado) {
        btnRegistrar.setEnabled(habilitado);
    }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    /** El walk-in se registró: se guarda el resultado y se cierra la ventana. */
    public void cerrarCon(ReservaResponseDTO reserva) {
        reservaCreada = reserva;
        dispose();
    }

    public Optional<ReservaResponseDTO> getReservaCreada() {
        return Optional.ofNullable(reservaCreada);
    }

    private void habilitarDatosCliente(boolean editable) {
        txtNombre.setEditable(editable);
        txtApellido.setEditable(editable);
        txtCelular.setEditable(editable);
    }
}