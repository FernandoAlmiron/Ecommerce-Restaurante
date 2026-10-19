package org.example.escritorio.controlador.caja;

import org.example.Modelo.reserva.Tarjeta;
import org.example.escritorio.Contexto;
import org.example.escritorio.vista.caja.TarjetaDialog;
import org.example.escritorio.vista.comun.Tarea;
import org.example.service.reserva.TarjetaService;

import java.time.YearMonth;

/**
 * Guarda una tarjeta nueva con TarjetaService (RF-46).
 * El formulario no deja elegir un vencimiento pasado; TarjetaService todavía no lo valida.
 */
public class TarjetaEdicionControlador {

    private final TarjetaDialog vista;
    private final TarjetaService tarjetaService;

    public TarjetaEdicionControlador(TarjetaDialog vista) {
        this.vista = vista;
        this.tarjetaService = Contexto.get(TarjetaService.class);
    }

    public void guardar() {
        if (vista.getTitular().isEmpty()) {
            vista.mostrarError("Completá el titular.");
            return;
        }
        if (vista.getVencimiento().isBefore(YearMonth.now())) {
            vista.mostrarError("La tarjeta está vencida.");
            return;
        }

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setTitular(vista.getTitular());
        tarjeta.setVencimiento(vista.getVencimiento());

        Tarea.ejecutar(vista.getBotonGuardar(), () -> tarjetaService.guardar(tarjeta), vista::cerrarCon);
    }
}