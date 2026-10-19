package org.example.escritorio.controlador.caja;

import org.example.escritorio.Contexto;
import org.example.escritorio.vista.caja.TarjetaDialog;
import org.example.escritorio.vista.caja.TarjetasPanel;
import org.example.escritorio.vista.comun.Tarea;
import org.example.service.reserva.TarjetaService;

import javax.swing.SwingUtilities;

/** Lógica de la lista de tarjetas (RF-46) con TarjetaService. */
public class TarjetasControlador {

    private final TarjetasPanel vista;
    private final TarjetaService tarjetaService;

    public TarjetasControlador(TarjetasPanel vista) {
        this.vista = vista;
        this.tarjetaService = Contexto.get(TarjetaService.class);
    }

    public void cargar() {
        Tarea.ejecutar(vista.getBotonActualizar(), tarjetaService::listarTodos, vista::mostrarTarjetas);
    }

    public void nueva() {
        TarjetaDialog dialogo = new TarjetaDialog(SwingUtilities.getWindowAncestor(vista));
        dialogo.setVisible(true); // es modal: sigue cuando se cierra
        dialogo.getGuardada().ifPresent(t -> {
            cargar();
            vista.mostrarInfo("Tarjeta N° " + t.getNroTarjeta() + " registrada a nombre de " + t.getTitular() + ".");
        });
    }
}