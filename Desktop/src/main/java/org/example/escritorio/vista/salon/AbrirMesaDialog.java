package org.example.escritorio.vista.salon;

import org.example.dto.ReservaResponseDTO;
import org.example.escritorio.controlador.salon.AbrirMesaControlador;
import org.example.escritorio.vista.comun.DialogoFormulario;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.comun.Formulario;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.Component;
import java.awt.Window;
import java.util.List;
import java.util.Optional;

/**
 * Ventana para abrir una mesa (RF-19): crea un ticket de salón vacío, con o sin reserva
 * (sin reserva se registra antes un walk-in, para que la mesa ocupe lugar en su zona).
 * Las reservas que se ofrecen son las de hoy (confirmadas o que ya asistieron) que todavía no tienen mesa.
 */
public class AbrirMesaDialog extends DialogoFormulario {

    private final JCheckBox chkConReserva = new JCheckBox("Viene con reserva");
    private final JComboBox<ReservaResponseDTO> cmbReserva = new JComboBox<>();
    private final JLabel lblAyuda = Formulario.nota(new JLabel(" "));

    private final AbrirMesaControlador controlador;
    private Integer ticketCreado;

    public AbrirMesaDialog(Window padre) {
        super(padre, "Salón", "Abrir mesa", "Se crea un ticket vacío para cargarle los pedidos.", "Abrir mesa");

        cmbReserva.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof ReservaResponseDTO r) {
                    setText("N° " + r.nroReserva() + " · " + Formatos.hora(r.hora()) + " · " + r.cliente()
                            + " · " + r.zona() + " · " + r.comensales() + " pers.");
                }
                return this;
            }
        });

        controlador = new AbrirMesaControlador(this);

        chkConReserva.addActionListener(e -> controlador.alCambiarConReserva());
        btnPrincipal.addActionListener(e -> controlador.abrir());

        cmbReserva.setEnabled(false);
        controlador.cargarReservas();

        armar();
    }

    @Override
    protected JComponent armarCuerpo() {
        Formulario.estilizar(cmbReserva);
        return Formulario.cuerpo(
                Formulario.opcion(chkConReserva, "Tildalo para asociar la mesa a una reserva de hoy."),
                Formulario.campo("Reserva", cmbReserva, null),
                lblAyuda);
    }

    // ---------- lo que usa el controlador ----------

    public boolean isConReserva() { return chkConReserva.isSelected(); }
    public ReservaResponseDTO getReservaSeleccionada() { return (ReservaResponseDTO) cmbReserva.getSelectedItem(); }

    public JButton getBotonAbrir() { return btnPrincipal; }
    public JComboBox<ReservaResponseDTO> getComboReserva() { return cmbReserva; }

    public void mostrarReservas(List<ReservaResponseDTO> reservas) {
        cmbReserva.removeAllItems();
        reservas.forEach(cmbReserva::addItem);
        if (reservas.isEmpty()) {
            chkConReserva.setSelected(false);
            chkConReserva.setEnabled(false);
            lblAyuda.setText("No hay reservas de hoy sin mesa: se pide la zona y los comensales.");
        } else {
            chkConReserva.setEnabled(true);
            lblAyuda.setText("Sin reserva: se pide la zona y los comensales para descontarlos del lugar libre.");
        }
        habilitarCombo(chkConReserva.isSelected());
    }

    public void habilitarCombo(boolean habilitado) {
        cmbReserva.setEnabled(habilitado && cmbReserva.getItemCount() > 0);
        // sin reserva todavía falta elegir zona y comensales, en la ventana siguiente
        btnPrincipal.setText(chkConReserva.isSelected() ? "Abrir mesa" : "Continuar");
    }

    /** El ticket se creó: se guarda su número y se cierra la ventana. */
    public void cerrarCon(int nroTicket) {
        ticketCreado = nroTicket;
        dispose();
    }

    public Optional<Integer> getTicketCreado() {
        return Optional.ofNullable(ticketCreado);
    }
}
