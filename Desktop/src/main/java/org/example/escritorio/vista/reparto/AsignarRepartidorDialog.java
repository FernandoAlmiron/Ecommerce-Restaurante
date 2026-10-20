package org.example.escritorio.vista.reparto;

import org.example.Modelo.persona.Empleado;
import org.example.dto.RemitoDTO;
import org.example.escritorio.controlador.reparto.AsignarRepartidorControlador;
import org.example.escritorio.vista.comun.DialogoFormulario;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formulario;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.util.List;

/** Asignar un repartidor a un envío (RF-36). Solo ADMIN; el combo muestra empleados del sector REPARTIDOR. */
public class AsignarRepartidorDialog extends DialogoFormulario {

    private final RemitoDTO envio;
    private final JComboBox<Empleado> cmbRepartidor = new JComboBox<>();

    private final AsignarRepartidorControlador controlador;
    private boolean asignado;

    public AsignarRepartidorDialog(Window padre, RemitoDTO envio) {
        super(padre, "Reparto · Envío #E-" + envio.idEnvio(), "Asignar repartidor", envio.direccionEntrega(), "Asignar");
        this.envio = envio;

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

        controlador = new AsignarRepartidorControlador(this);
        btnPrincipal.addActionListener(e -> controlador.asignar());
        controlador.cargarRepartidores();

        armar();
    }

    @Override
    protected JComponent armarCuerpo() {
        Formulario.estilizar(cmbRepartidor);
        JLabel actual = new JLabel(envio.repartidor());
        actual.setFont(Estilo.fuente(Font.BOLD, 16f));
        actual.setForeground("Sin asignar".equals(envio.repartidor()) ? Estilo.ROJO_TEXTO : Estilo.TEXTO);
        return Formulario.cuerpo(
                Formulario.campo("Repartidor actual", actual, null),
                Formulario.campo("Nuevo repartidor", cmbRepartidor, "Solo aparecen los empleados del sector Reparto."));
    }

    // ---------- lo que usa el controlador ----------

    public RemitoDTO getEnvio() { return envio; }
    public Empleado getRepartidorElegido() { return (Empleado) cmbRepartidor.getSelectedItem(); }
    public JButton getBotonAsignar() { return btnPrincipal; }
    public JComboBox<Empleado> getComboRepartidor() { return cmbRepartidor; }

    public void mostrarRepartidores(List<Empleado> repartidores) {
        cmbRepartidor.removeAllItems();
        repartidores.forEach(cmbRepartidor::addItem);
        btnPrincipal.setEnabled(!repartidores.isEmpty());
    }

    public void cerrarAsignado() {
        asignado = true;
        dispose();
    }

    public boolean seAsigno() {
        return asignado;
    }
}