package org.example.escritorio.controlador.admin;

import org.example.Modelo.persona.Sector;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.admin.SectorForm;
import org.example.escritorio.vista.admin.SectoresPanel;
import org.example.escritorio.vista.comun.FormularioDialog;
import org.example.escritorio.vista.comun.Tarea;
import org.example.service.RestauranteService;
import org.example.service.persona.EmpleadoService;
import org.example.service.persona.SectorService;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.Callable;

/**
 * Sectores del restaurante con SectorService (RF-52).
 * El backend asigna el rol según el nombre del sector, por eso cambiar un nombre se confirma aparte.
 */
public class SectoresControlador {

    private final SectoresPanel vista;
    private final SectorService sectorService;
    private final RestauranteService restauranteService;
    private final EmpleadoService empleadoService;
    private List<Sector> sectores = List.of(); // lo último que se cargó, para avisar nombres repetidos

    public SectoresControlador(SectoresPanel vista) {
        this.vista = vista;
        this.sectorService = Contexto.get(SectorService.class);
        this.restauranteService = Contexto.get(RestauranteService.class);
        this.empleadoService = Contexto.get(EmpleadoService.class);
    }

    /** Lo que muestra la pantalla: los sectores y cuántos empleados tiene cada uno. */
    private record Datos(List<Sector> sectores, Map<Integer, Long> empleadosPorSector) {
    }

    /** @param seleccionar id del sector a dejar seleccionado, o null para mantener el actual */
    public void cargar(Integer seleccionar) {
        Integer aSeleccionar = seleccionar != null ? seleccionar
                : vista.seleccionado().map(Sector::getIdSector).orElse(null);
        int restaurante = Sesion.nroRestaurante();

        Tarea.ejecutar(vista.getBotonActualizar(),
                () -> new Datos(
                        sectorService.listarTodos().stream()
                                .filter(s -> s.getRestaurante() == null
                                        || s.getRestaurante().getNroRestaurante() == restaurante)
                                .toList(),
                        // cuántos empleados hay en cada sector (un sector con empleados no se puede borrar)
                        empleadoService.listarTodos().stream()
                                .filter(e -> e.getSector() != null)
                                .collect(Collectors.groupingBy(e -> e.getSector().getIdSector(), Collectors.counting()))),
                datos -> {
                    List<Sector> lista = datos.sectores();
                    sectores = lista;
                    vista.setEmpleadosPorSector(datos.empleadosPorSector());
                    long activos = lista.stream().filter(Sector::isActiva).count();
                    vista.mostrar(lista, lista.size() + " sectores · " + activos + " activos");
                    if (aSeleccionar != null) {
                        vista.seleccionar(s -> s.getIdSector() == aSeleccionar);
                    }
                    alCambiarSeleccion();
                });
    }

    public void alCambiarSeleccion() {
        Sector s = vista.seleccionado().orElse(null);
        vista.habilitarAcciones(s != null);
        vista.mostrarBotonActivar(s);
    }

    public void nuevo() {
        abrir(null);
    }

    public void modificar() {
        vista.seleccionado().ifPresent(this::abrir);
    }

    /** Un sector inactivo no se ofrece al dar de alta o cambiar de sector a un empleado. */
    public void activarODesactivar() {
        vista.seleccionado().ifPresent(sector -> {
            boolean activar = !sector.isActiva();
            if (!activar && !vista.confirmar("¿Desactivar el sector " + sector.getNombre() + "?\n"
                    + "No se va a poder asignar a empleados. Los que ya están en el sector no cambian.")) {
                cargar(null); // el interruptor de la fila vuelve a como estaba
                return;
            }
            Tarea.ejecutar(vista.getBotonActivar(),
                    () -> activar ? sectorService.activar(sector.getIdSector())
                            : sectorService.desactivar(sector.getIdSector()),
                    cambiado -> cargar(cambiado.getIdSector()));
        });
    }

    public void eliminar() {
        vista.seleccionado().ifPresent(sector -> {
            if (!vista.confirmar("¿Eliminar el sector " + sector.getNombre() + "?")) {
                return;
            }
            Tarea.ejecutar(vista.getBotonEliminar(), () -> {
                try {
                    sectorService.eliminar(sector.getIdSector());
                    return true;
                } catch (DataIntegrityViolationException e) {
                    throw new IllegalStateException("El sector " + sector.getNombre() + " tiene empleados y no se "
                            + "puede eliminar. Pasalos a otro sector en Recursos humanos o desactivalo.");
                }
            }, ok -> cargar(null));
        });
    }

    private void abrir(Sector original) {
        SectorForm form = new SectorForm(original);
        FormularioDialog.abrir(vista, original == null ? "Nuevo sector" : "Modificar sector", form,
                () -> preparar(form, original)
        ).ifPresent(guardado -> cargar(guardado.getIdSector()));
    }

    /** Lee y valida el formulario (en el hilo de Swing). Devuelve null si el usuario no confirma. */
    private Callable<Sector> preparar(SectorForm form, Sector original) {
        String nombre = form.getNombre();
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("Completá el nombre del sector.");
        }
        boolean repetido = sectores.stream().anyMatch(s -> s.getNombre() != null
                && s.getNombre().equalsIgnoreCase(nombre)
                && (original == null || s.getIdSector() != original.getIdSector()));
        if (repetido) {
            throw new IllegalArgumentException("Ya hay un sector llamado \"" + nombre + "\".");
        }
        if (original != null && !nombre.equals(original.getNombre())
                && !vista.confirmar("¿Cambiar el nombre de " + original.getNombre() + " a " + nombre + "?\n"
                + "Los empleados de este sector pueden ver otras pantallas (o ninguna) la próxima vez "
                + "que inicien sesión, porque los permisos dependen del nombre.")) {
            return null;
        }

        Sector datos = new Sector();
        datos.setNombre(nombre);
        datos.setActiva(original == null || original.isActiva());
        int restaurante = Sesion.nroRestaurante();

        return () -> {
            if (original != null) {
                return sectorService.actualizar(original.getIdSector(), datos);
            }
            datos.setRestaurante(restauranteService.buscarPorId(restaurante));
            return sectorService.guardar(datos);
        };
    }
}