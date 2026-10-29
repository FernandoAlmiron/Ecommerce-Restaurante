package org.example.escritorio.vista.recepcion;

import org.example.Modelo.persona.Cliente;
import org.example.Modelo.reserva.Zona;
import org.example.dto.ReservaResponseDTO;
import org.example.escritorio.controlador.recepcion.WalkInControlador;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.DialogoFormulario;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formatos;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Ventana para ubicar a un cliente que llega sin reserva (RF-17).
 * Primero se busca por DNI: si existe se usan sus datos; si no, se completan nombre y apellido.
 * La usan Recepción ("Nuevo walk-in") y Salón (abrir una mesa sin reserva).
 * En Recepción además se elige el horario de hoy: ahora o más tarde, cada 15 minutos hasta el cierre;
 * en Salón no, porque la mesa se abre en el momento.
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
    private final JComboBox<Horario> cmbHorario = new JComboBox<>();
    private final boolean conHorario;

    /** Un horario de hoy para el walk-in; sin hora es "ahora". */
    public record Horario(LocalTime hora) {
        @Override
        public String toString() {
            return hora == null ? "Ahora (" + Formatos.hora(LocalTime.now()) + ")" : Formatos.hora(hora);
        }
    }

    private final WalkInControlador controlador;
    private ReservaResponseDTO reservaCreada;
    /** Lugares libres ahora en cada zona (por id), para mostrarlos en el combo. */
    private Map<Integer, Integer> libres = Map.of();

    /** Recepción: con el horario (ahora o más tarde, hoy). */
    public WalkInDialog(Window padre) {
        this(padre, "Recepción", "Nuevo walk-in", "Cliente sin reserva: para ahora o más tarde, hoy.",
                "Registrar walk-in", true);
    }

    /** Sin elegir horario: es para ahora (Salón, al abrir una mesa sin reserva). */
    public WalkInDialog(Window padre, String seccion, String titulo, String subtitulo, String accionPrincipal) {
        this(padre, seccion, titulo, subtitulo, accionPrincipal, false);
    }

    private WalkInDialog(Window padre, String seccion, String titulo, String subtitulo, String accionPrincipal,
                         boolean conHorario) {
        super(padre, seccion, titulo, subtitulo, accionPrincipal);
        this.conHorario = conHorario;
        cmbHorario.addItem(new Horario(null));
        cmbHorario.setMaximumRowCount(8); // se abre como una lista con scroll

        cmbZona.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof Zona z) {
                    Integer quedan = libres.get(z.getIdZona());
                    setText(quedan == null ? z.getNombre() + " (hasta " + z.getCapacidadMaxima() + ")"
                            : z.getNombre() + " · " + quedan + " libres de " + z.getCapacidadMaxima());
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
        // los lugares libres dependen del horario: se vuelven a contar al cambiarlo
        cmbHorario.addActionListener(e -> controlador.cargarZonas());

        limpiarCliente();
        controlador.cargarZonas();
        if (conHorario) {
            controlador.cargarHorarios();
        }

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
        Formulario.estilizar(cmbHorario);
        btnBuscar.setBorder(Estilo.relleno(10, 16, 10, 18));

        // el DNI con su botón Buscar al lado y, abajo, si el cliente ya existe o es nuevo
        JPanel filaDni = Estilo.transparente(new BorderLayout(10, 0));
        filaDni.add(txtDni, BorderLayout.CENTER);
        filaDni.add(btnBuscar, BorderLayout.EAST);
        JPanel dni = Estilo.transparente(new BorderLayout(0, 8));
        dni.add(filaDni, BorderLayout.CENTER);
        dni.add(lblCliente, BorderLayout.SOUTH);

        JComponent zonaYComensales = Formulario.enFila(
                Formulario.campo("Zona", cmbZona, null),
                Formulario.campo("Comensales", spnComensales, null));
        JComponent cliente = Formulario.enFila(
                Formulario.campo("Nombre", txtNombre, null),
                Formulario.campo("Apellido", txtApellido, null));
        if (!conHorario) {
            return Formulario.cuerpo(Formulario.campo("DNI", dni, null), cliente,
                    Formulario.campo("Celular", txtCelular, null), zonaYComensales);
        }
        return Formulario.cuerpo(Formulario.campo("DNI", dni, null), cliente,
                Formulario.campo("Celular", txtCelular, null),
                Formulario.campo("Horario", cmbHorario, "Hoy, cada 15 minutos hasta el cierre del restaurante."),
                zonaYComensales);
    }

    // ---------- lo que usa el controlador ----------

    public String getDniTexto() { return txtDni.getText().trim(); }
    public String getNombre() { return txtNombre.getText().trim(); }
    public String getApellido() { return txtApellido.getText().trim(); }
    public String getCelular() { return txtCelular.getText().trim(); }
    public int getComensales() { return (Integer) spnComensales.getValue(); }
    public Zona getZonaSeleccionada() { return (Zona) cmbZona.getSelectedItem(); }

    /** El horario elegido; null es "ahora" (y siempre es ahora en Salón). */
    public LocalTime getHora() {
        Horario h = (Horario) cmbHorario.getSelectedItem();
        return h == null ? null : h.hora();
    }

    public JButton getBotonBuscar() { return btnBuscar; }
    public JButton getBotonRegistrar() { return btnPrincipal; }
    public JComboBox<Zona> getComboZona() { return cmbZona; }
    public JComboBox<Horario> getComboHorario() { return cmbHorario; }

    /** "Ahora" y los horarios que quedan hoy. */
    public void mostrarHorarios(List<LocalTime> horarios) {
        cmbHorario.removeAllItems();
        cmbHorario.addItem(new Horario(null));
        horarios.forEach(h -> cmbHorario.addItem(new Horario(h)));
    }

    /** Las zonas con sus lugares libres; si ya había una elegida, queda elegida. */
    public void mostrarZonas(List<Zona> zonas, Map<Integer, Integer> libres) {
        this.libres = libres;
        Zona antes = getZonaSeleccionada();
        cmbZona.removeAllItems();
        zonas.forEach(cmbZona::addItem);
        if (antes != null) {
            zonas.stream().filter(z -> z.getIdZona() == antes.getIdZona()).findFirst().ifPresent(cmbZona::setSelectedItem);
        }
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
