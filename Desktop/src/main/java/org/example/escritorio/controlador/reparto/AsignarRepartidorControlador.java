package org.example.escritorio.controlador.reparto;

import org.example.Modelo.persona.Empleado;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.comun.Tarea;
import org.example.escritorio.vista.reparto.AsignarRepartidorDialog;
import org.example.service.persona.EmpleadoService;
import org.example.service.reserva.EnvioService;

import java.util.Comparator;

/**
 * Asigna un repartidor a un envío (RF-36) con EnvioService.asignarRepartidor, que valida
 * que el empleado sea del sector REPARTIDOR y que el envío no esté entregado ni cancelado.
 */
public class AsignarRepartidorControlador {

    private final AsignarRepartidorDialog vista;
    private final EnvioService envioService;
    private final EmpleadoService empleadoService;

    public AsignarRepartidorControlador(AsignarRepartidorDialog vista) {
        this.vista = vista;
        this.envioService = Contexto.get(EnvioService.class);
        this.empleadoService = Contexto.get(EmpleadoService.class);
    }

    /** Empleados del sector REPARTIDOR del restaurante del empleado logueado. */
    public void cargarRepartidores() {
        int restaurante = Sesion.nroRestaurante();
        Tarea.ejecutar(vista.getComboRepartidor(),
                () -> empleadoService.listarTodos().stream()
                        .filter(e -> e.getSector() != null && "REPARTIDOR".equalsIgnoreCase(e.getSector().getNombre()))
                        .filter(e -> e.getSector().getRestaurante() == null
                                || e.getSector().getRestaurante().getNroRestaurante() == restaurante)
                        .sorted(Comparator.comparing(Empleado::getApellido))
                        .toList(),
                vista::mostrarRepartidores);
    }

    public void asignar() {
        Empleado repartidor = vista.getRepartidorElegido();
        if (repartidor == null) {
            vista.mostrarError("Elegí el repartidor.");
            return;
        }
        int idEnvio = vista.getEnvio().idEnvio();
        Tarea.ejecutar(vista.getBotonAsignar(),
                () -> envioService.asignarRepartidor(idEnvio, repartidor.getLegajo()),
                envio -> vista.cerrarAsignado());
    }
}