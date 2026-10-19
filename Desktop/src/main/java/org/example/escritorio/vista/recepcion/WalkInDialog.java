package org.example.escritorio.vista.recepcion;

import org.example.Modelo.persona.Cliente;
import org.example.Modelo.reserva.Zona;
import org.example.dto.ReservaResponseDTO;
import org.example.escritorio.controlador.recepcion.WalkInControlador;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.DialogoFormulario;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formulario;
import org.example.escritorio.vista.comun.Icono;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import java.awt.Window;
import java.util.List;
import java.util.Optional;

/**
 * Ventana para ubicar a un cliente que llega sin reserva (RF-17).
 * Primero se busca por DNI: si existe se usan sus datos; si no, se completan nombre y apellido.
 */
public class WalkInDialog extends DialogoFormulario {

    private final JTextField txtDni = new JTextField(10);
    private final JButton btnBuscar = new Boton("Buscar", new Icono(Icono.Tipo.LUPA, 16, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);
    private final JLabel lblCliente = Formulario.nota(new JLabel("Ingresá el DNI y tocá Buscar."));
    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtApellido = new JTextField(20);
    private final JTextField txtCelular = new JTextField(20);
    private final JComboBox<Zona> cmbZona = new JComboBox<>();
    private final JSpinner spnComensales = new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));

    private final WalkInControlador controlador;
    private ReservaResponseDTO reservaCreada;

    public WalkInDialog(Window padre) {
        super(padre, "Recepción", "Nuevo walk-in", "Cliente que llega sin reserva.", "Registrar walk-in");

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
        btnPrincipal.addActionListener(e -> controlador.registrar());

        limpiarCliente();
        controlador.cargarZonas();

        armar();
    }

    @Override
    protected JComponent armarCuerpo() {
        Formulario.estilizar(txtDni, "Sin puntos");
        Formulario.estilizar(txtNombre, null);
        Formulario.estilizar(txtApellido, null);
        Formulario.estilizar(txtCelular, "Opcional");
        Formulario.estilizar(cmbZona);
        Formulario.estilizar(spnComensales);
        btnBuscar.setBorder(Estilo.relleno(10, 16, 10, 18));

        // el DNI con su botón Buscar al lado y, abajo, si el cliente ya existe o es nuevo
        JPanel filaDni = Estilo.transparente(new BorderLayout(10, 0));
        filaDni.add(txtDni, BorderLayout.CENTER);
        filaDni.add(btnBuscar, BorderLayout.EAST);
        JPanel dni = Estilo.transparente(new BorderLayout(0, 8));
        dni.add(filaDni, BorderLayout.CENTER);
        dni.add(lblCliente, BorderLayout.SOUTH);

        return Formulario.cuerpo(
                Formulario.campo("DNI", dni, null),
                Formulario.enFila(
                        Formulario.campo("Nombre", txtNombre, null),
                        Formulario.campo("Apellido", txtApellido, null)),
                Formulario.campo("Celular", txtCelular, null),
                Formulario.enFila(
                        Formulario.campo("Zona", cmbZona, null),
                        Formulario.campo("Comensales", spnComensales, null)));
    }

    // ---------- lo que usa el controlador ----------

    public String getDniTexto() { return txtDni.getText().trim(); }
    public String getNombre() { return txtNombre.getText().trim(); }
    public String getApellido() { return txtApellido.getText().trim(); }
    public String getCelular() { return txtCelular.getText().trim(); }
    public int getComensales() { return (Integer) spnComensales.getValue(); }
    public Zona getZonaSeleccionada() { return (Zona) cmbZona.getSelectedItem(); }

    public JButton getBotonBuscar() { return btnBuscar; }
    public JButton getBotonRegistrar() { return btnPrincipal; }
    public JComboBox<Zona> getComboZona() { return cmbZona; }

    public void mostrarZonas(List<Zona> zonas) {
        cmbZona.removeAllItems();
        zonas.forEach(cmbZona::addItem);
    }

    public void mostrarClienteExistente(Cliente cliente) {
        avisarCliente("Cliente registrado: se usan sus datos.", Estilo.VERDE_TEXTO);
        txtNombre.setText(cliente.getNombre());
        txtApellido.setText(cliente.getApellido());
        txtCelular.setText(cliente.getCelular() == null ? "" : cliente.getCelular());
        habilitarDatosCliente(false);
        cmbZona.requestFocusInWindow();
    }

    public void prepararClienteNuevo() {
        avisarCliente("Cliente nuevo: cargá sus datos.", Estilo.AMARILLO_TEXTO);
        txtNombre.setText("");
        txtApellido.setText("");
        txtCelular.setText("");
        habilitarDatosCliente(true);
        txtNombre.requestFocusInWindow();
    }

    public void limpiarCliente() {
        avisarCliente("Ingresá el DNI y tocá Buscar.", Estilo.TEXTO_SUAVE);
        txtNombre.setText("");
        txtApellido.setText("");
        txtCelular.setText("");
        habilitarDatosCliente(false);
        btnPrincipal.setEnabled(false);
    }

    public void habilitarRegistrar(boolean habilitado) {
        btnPrincipal.setEnabled(habilitado);
    }

    /** El walk-in se registró: se guarda el resultado y se cierra la ventana. */
    public void cerrarCon(ReservaResponseDTO reserva) {
        reservaCreada = reserva;
        dispose();
    }

    public Optional<ReservaResponseDTO> getReservaCreada() {
        return Optional.ofNullable(reservaCreada);
    }

    private void avisarCliente(String texto, java.awt.Color color) {
        lblCliente.setText(texto);
        lblCliente.setForeground(color);
    }

    private void habilitarDatosCliente(boolean editable) {
        txtNombre.setEditable(editable);
        txtApellido.setEditable(editable);
        txtCelular.setEditable(editable);
    }
}
