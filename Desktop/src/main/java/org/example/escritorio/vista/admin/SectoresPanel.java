package org.example.escritorio.vista.admin;

import org.example.Modelo.persona.Sector;
import org.example.escritorio.controlador.admin.SectoresControlador;
import org.example.escritorio.vista.comun.PanelAbm;

import javax.swing.JButton;

/** Solapa Sectores de Administración (RF-52): alta, modificación, activar/desactivar y baja. */
public class SectoresPanel extends PanelAbm<Sector> {

    private final JButton btnActivar = new JButton("Desactivar");

    public SectoresPanel() {
        super(new SectorTableModel(), 0, new int[]{300, 80}, "Nuevo sector");
        agregarBoton(btnActivar);

        SectoresControlador controlador = new SectoresControlador(this);
        btnActivar.addActionListener(e -> controlador.activarODesactivar());
        conectar(() -> controlador.cargar(null), controlador::nuevo, controlador::modificar,
                controlador::eliminar, controlador::alCambiarSeleccion);
        controlador.cargar(null);
    }

    /** El botón dice Activar o Desactivar según el sector elegido; sin selección queda apagado. */
    public void mostrarBotonActivar(Sector sector) {
        btnActivar.setEnabled(sector != null);
        btnActivar.setText(sector != null && !sector.isActiva() ? "Activar" : "Desactivar");
    }

    public JButton getBotonActivar() { return btnActivar; }
}