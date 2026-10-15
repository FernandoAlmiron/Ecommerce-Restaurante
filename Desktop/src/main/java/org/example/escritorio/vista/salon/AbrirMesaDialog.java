package org.example.escritorio.vista.salon;

import org.example.dto.ReservaResponseDTO;
import org.example.escritorio.controlador.salon.AbrirMesaControlador;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Formatos;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
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
 * Ventana para abrir una mesa (RF-19): crea un ticket de salón vacío, con o sin reserva.
 * Las reservas que se ofrecen son las confirmadas de hoy que todavía no tienen mesa abierta.
 */
public class AbrirMesaDialog extends JDialog {

    private final JCheckBox chkConReserva = new JCheckBox("Viene con reserva");
    private final JComboBox<ReservaResponseDTO> cmbReserva = new JComboBox<>();
    private final JLabel lblAyuda = new JLabel(" ");
    private final JButton btnAbrir = new JButton("Abrir mesa");
    private final JButton btnCancelar = new JButton("Cancelar");

    private final AbrirMesaControlador controlador;
    private Integer ticketCreado;

    public AbrirMesaDialog(Window padre) {
        super(padre, "Abrir mesa", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        raiz.add(armarFormulario(), BorderLayout.CENTER);
        raiz.add(armarBotones(), BorderLayout.SOUTH);
        setContentPane(raiz);

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
        btnAbrir.addActionListener(e -> controlador.abrir());
        btnCancelar.addActionListener(e -> dispose());

        cmbReserva.setEnabled(false);
        controlador.cargarReservas();

        pack();
        setSize(Math.max(getWidth(), 520), getHeight());
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    private JPanel armarFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.insets = new Insets(4, 0, 4, 0);

        JLabel titulo = new JLabel("Nueva mesa");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        c.gridy = 0;
        panel.add(titulo, c);

        c.gridy = 1;
        panel.add(chkConReserva, c);

        c.gridy = 2;
        panel.add(cmbReserva, c);

        lblAyuda.setFont(lblAyuda.getFont().deriveFont(Font.ITALIC));
        c.gridy = 3;
        panel.add(lblAyuda, c);
        return panel;
    }

    private JPanel armarBotones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnCancelar);
        botones.add(btnAbrir);
        getRootPane().setDefaultButton(btnAbrir);
        return botones;
    }

    // ---------- lo que usa el controlador ----------

    public boolean isConReserva() { return chkConReserva.isSelected(); }
    public ReservaResponseDTO getReservaSeleccionada() { return (ReservaResponseDTO) cmbReserva.getSelectedItem(); }

    public JButton getBotonAbrir() { return btnAbrir; }
    public JComboBox<ReservaResponseDTO> getComboReserva() { return cmbReserva; }

    public void mostrarReservas(List<ReservaResponseDTO> reservas) {
        cmbReserva.removeAllItems();
        reservas.forEach(cmbReserva::addItem);
        if (reservas.isEmpty()) {
            chkConReserva.setSelected(false);
            chkConReserva.setEnabled(false);
            lblAyuda.setText("No hay reservas confirmadas de hoy sin mesa: se abre sin reserva.");
        } else {
            chkConReserva.setEnabled(true);
            lblAyuda.setText("Sin reserva: la mesa se abre igual, sin cliente asociado.");
        }
        habilitarCombo(chkConReserva.isSelected());
    }

    public void habilitarCombo(boolean habilitado) {
        cmbReserva.setEnabled(habilitado && cmbReserva.getItemCount() > 0);
    }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
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