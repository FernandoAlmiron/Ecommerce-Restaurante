package org.example.escritorio.vista.reparto;

import org.example.Modelo.persona.Empleado;
import org.example.dto.RemitoDTO;
import org.example.escritorio.controlador.reparto.AsignarRepartidorControlador;
import org.example.escritorio.vista.comun.Dialogos;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.List;

/** Asignar un repartidor a un envío (RF-36). Solo ADMIN; el combo muestra empleados del sector REPARTIDOR. */
public class AsignarRepartidorDialog extends JDialog {

    private final RemitoDTO envio;
    private final JComboBox<Empleado> cmbRepartidor = new JComboBox<>();
    private final JButton btnAsignar = new JButton("Asignar");
    private final JButton btnCancelar = new JButton("Cancelar");

    private final AsignarRepartidorControlador controlador;
    private boolean asignado;

    public AsignarRepartidorDialog(Window padre, RemitoDTO envio) {
        super(padre, "Asignar repartidor — Envío N° " + envio.idEnvio(), ModalityType.APPLICATION_MODAL);
        this.envio = envio;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        cmbRepartidor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof Empleado e) {
                    setText(e.getNombre() + " " + e.getApellido() + " (legajo " + e.getLegajo() + ")");
                }
                return this;
            }
        });

        JLabel titulo = new JLabel("Envío N° " + envio.idEnvio() + " · " + envio.direccionEntrega());
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 15f));

        JPanel datos = new JPanel(new GridLayout(0, 2, 12, 6));
        datos.add(new JLabel("Repartidor actual"));
        datos.add(new JLabel(envio.repartidor()));
        datos.add(new JLabel("Nuevo repartidor"));
        datos.add(cmbRepartidor);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnCancelar);
        botones.add(btnAsignar);
        getRootPane().setDefaultButton(btnAsignar);

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        raiz.add(titulo, BorderLayout.NORTH);
        raiz.add(datos, BorderLayout.CENTER);
        raiz.add(botones, BorderLayout.SOUTH);
        setContentPane(raiz);

        controlador = new AsignarRepartidorControlador(this);
        btnAsignar.addActionListener(e -> controlador.asignar());
        btnCancelar.addActionListener(e -> dispose());
        controlador.cargarRepartidores();

        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    // ---------- lo que usa el controlador ----------

    public RemitoDTO getEnvio() { return envio; }
    public Empleado getRepartidorElegido() { return (Empleado) cmbRepartidor.getSelectedItem(); }
    public JButton getBotonAsignar() { return btnAsignar; }
    public JComboBox<Empleado> getComboRepartidor() { return cmbRepartidor; }

    public void mostrarRepartidores(List<Empleado> repartidores) {
        cmbRepartidor.removeAllItems();
        repartidores.forEach(cmbRepartidor::addItem);
        btnAsignar.setEnabled(!repartidores.isEmpty());
    }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public void cerrarAsignado() {
        asignado = true;
        dispose();
    }

    public boolean seAsigno() {
        return asignado;
    }
}