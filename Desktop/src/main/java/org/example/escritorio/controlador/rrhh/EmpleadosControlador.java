package org.example.escritorio.controlador.rrhh;

import org.example.Modelo.persona.Empleado;
import org.example.Modelo.persona.Sector;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.comun.Tarea;
import org.example.escritorio.vista.rrhh.CambiarSectorDialog;
import org.example.escritorio.vista.rrhh.EmpleadoDialog;
import org.example.escritorio.vista.rrhh.EmpleadoTableModel;
import org.example.escritorio.vista.rrhh.EmpleadosPanel;
import org.example.service.persona.EmpleadoService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

import javax.swing.SwingUtilities;
import java.util.List;

/**
 * Lógica de Recursos humanos con EmpleadoService (RF-53 a RF-55).
 * Todos los métodos del service reciben si quien opera es ADMIN; con eso el backend protege ADMINISTRACION.
 */
public class EmpleadosControlador {

    private final EmpleadosPanel vista;
    private final EmpleadoService empleadoService;

    public EmpleadosControlador(EmpleadosPanel vista) {
        this.vista = vista;
        this.empleadoService = Contexto.get(EmpleadoService.class);
    }

    /**
     * Los empleados del restaurante del usuario logueado.
     * @param seleccionar legajo a dejar seleccionado, o null para mantener el actual
     */
    public void cargar(Integer seleccionar) {
        Integer aSeleccionar = seleccionar != null ? seleccionar
                : vista.empleadoSeleccionado().map(Empleado::getLegajo).orElse(null);
        int restaurante = Sesion.nroRestaurante();

        Tarea.ejecutar(vista.getBotonActualizar(),
                () -> empleadoService.listarTodos().stream()
                        .filter(e -> e.getSector() == null || e.getSector().getRestaurante() == null
                                || e.getSector().getRestaurante().getNroRestaurante() == restaurante)
                        .toList(),
                empleados -> {
                    vista.mostrarEmpleados(empleados);
                    if (aSeleccionar != null) {
                        vista.seleccionarEmpleado(aSeleccionar);
                    }
                    alCambiarSeleccion();
                });
    }

    /** RRHH no puede tocar a los de ADMINISTRACION (RF-55): para ellos los botones quedan apagados. */
    public void alCambiarSeleccion() {
        Empleado e = vista.empleadoSeleccionado().orElse(null);
        vista.habilitarAcciones(e != null && (Sesion.esAdmin() || !EmpleadoTableModel.esAdministracion(e)));
    }

    public void nuevo() {
        abrirEdicion(null);
    }

    public void modificar() {
        vista.empleadoSeleccionado().ifPresent(this::abrirEdicion);
    }

    /** RF-54: el sector se cambia aparte de los datos personales. */
    public void cambiarSector() {
        vista.empleadoSeleccionado().ifPresent(empleado -> {
            boolean admin = Sesion.esAdmin();
            Tarea.ejecutar(vista.getBotonCambiarSector(), SectoresDisponibles::listar, sectores -> {
                List<Sector> otros = sectores.stream()
                        .filter(s -> empleado.getSector() == null || s.getIdSector() != empleado.getSector().getIdSector())
                        .toList();
                CambiarSectorDialog.pedirSector(vista, empleado, otros).ifPresent(nuevo ->
                        Tarea.ejecutar(vista.getBotonCambiarSector(), () -> {
                            try {
                                return empleadoService.cambiarSector(empleado.getLegajo(), nuevo, admin);
                            } catch (AccessDeniedException ex) {
                                throw new IllegalStateException("Solo un administrador puede mover empleados de o hacia ADMINISTRACION.");
                            }
                        }, cambiado -> {
                            cargar(cambiado.getLegajo());
                            vista.mostrarInfo(cambiado.getApellido() + ", " + cambiado.getNombre()
                                    + " pasó a " + nuevo.getNombre()
                                    + ". El cambio de pantallas se ve la próxima vez que inicie sesión.");
                        }));
            });
        });
    }

    /** RF-54. El backend borra el registro; si tiene cierres de caja o envíos, la base no lo deja. */
    public void darDeBaja() {
        vista.empleadoSeleccionado().ifPresent(empleado -> {
            if (empleado.getUsername() != null && empleado.getUsername().equals(Sesion.usuario())) {
                vista.mostrarError("No podés darte de baja a vos mismo.");
                return;
            }
            if (!vista.confirmar("¿Dar de baja a " + empleado.getApellido() + ", " + empleado.getNombre()
                    + " (legajo " + empleado.getLegajo() + ")? Ya no va a poder iniciar sesión.")) {
                return;
            }
            boolean admin = Sesion.esAdmin();
            Tarea.ejecutar(vista.getBotonBaja(), () -> {
                try {
                    empleadoService.eliminar(empleado.getLegajo(), admin);
                    return true;
                } catch (AccessDeniedException ex) {
                    throw new IllegalStateException("Solo un administrador puede dar de baja a alguien de ADMINISTRACION.");
                } catch (DataIntegrityViolationException ex) {
                    throw new IllegalStateException("No se puede dar de baja a " + empleado.getApellido()
                            + " porque tiene cierres de caja o envíos registrados.");
                }
            }, ok -> cargar(null));
        });
    }

    private void abrirEdicion(Empleado empleado) {
        EmpleadoDialog dialogo = new EmpleadoDialog(SwingUtilities.getWindowAncestor(vista), empleado);
        dialogo.setVisible(true); // es modal: sigue cuando se cierra
        dialogo.getGuardado().ifPresent(guardado -> cargar(guardado.getLegajo()));
    }
}