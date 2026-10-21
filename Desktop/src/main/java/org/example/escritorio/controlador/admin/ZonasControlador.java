package org.example.escritorio.controlador.admin;

import org.example.Modelo.reserva.Zona;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.admin.ZonaForm;
import org.example.escritorio.vista.admin.ZonasPanel;
import org.example.escritorio.vista.comun.FormularioDialog;
import org.example.escritorio.vista.comun.Tarea;
import org.example.service.RestauranteService;
import org.example.service.reserva.ZonaService;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.concurrent.Callable;

/** Alta, modificación y baja de las zonas del restaurante con ZonaService (RF-51). */
public class ZonasControlador {

    private final ZonasPanel vista;
    private final ZonaService zonaService;
    private final RestauranteService restauranteService;
    private List<Zona> zonas = List.of(); // lo último que se cargó, para avisar nombres repetidos

    public ZonasControlador(ZonasPanel vista) {
        this.vista = vista;
        this.zonaService = Contexto.get(ZonaService.class);
        this.restauranteService = Contexto.get(RestauranteService.class);
    }

    /** @param seleccionar id de la zona a dejar seleccionada, o null para mantener la actual */
    public void cargar(Integer seleccionar) {
        Integer aSeleccionar = seleccionar != null ? seleccionar
                : vista.seleccionado().map(Zona::getIdZona).orElse(null);
        int restaurante = Sesion.nroRestaurante();

        Tarea.ejecutar(vista.getBotonActualizar(),
                () -> zonaService.listarTodos().stream()
                        .filter(z -> z.getRestaurante() == null
                                || z.getRestaurante().getNroRestaurante() == restaurante)
                        .toList(),
                lista -> {
                    zonas = lista;
                    int capacidad = lista.stream().filter(Zona::isActiva).mapToInt(Zona::getCapacidadMaxima).sum();
                    long activas = lista.stream().filter(Zona::isActiva).count();
                    vista.mostrar(lista, lista.size() + " zonas · " + activas + " activas · capacidad activa: "
                            + capacidad + " personas");
                    if (aSeleccionar != null) {
                        vista.seleccionar(z -> z.getIdZona() == aSeleccionar);
                    }
                    alCambiarSeleccion();
                });
    }

    public void alCambiarSeleccion() {
        vista.habilitarAcciones(vista.seleccionado().isPresent());
    }

    public void nuevo() {
        abrir(null);
    }

    public void modificar() {
        vista.seleccionado().ifPresent(this::abrir);
    }

    /**
     * Habilita o deshabilita la zona elegida (el interruptor de su tarjeta). Una zona deshabilitada no se borra:
     * deja de ofrecerse para reservas nuevas. Se guarda con el mismo ZonaService.actualizar que usa Modificar.
     */
    public void activarODesactivar() {
        vista.seleccionado().ifPresent(zona -> {
            boolean activar = !zona.isActiva();
            if (!activar && !vista.confirmar("¿Deshabilitar la zona \"" + zona.getNombre() + "\"?\n"
                    + "No se va a ofrecer para reservas nuevas. Las reservas que ya tiene no cambian.")) {
                cargar(null); // el interruptor vuelve a como estaba
                return;
            }
            Zona datos = new Zona();
            datos.setNombre(zona.getNombre());
            datos.setCapacidadMaxima(zona.getCapacidadMaxima());
            datos.setActiva(activar);
            Tarea.ejecutar(vista.getBotonActualizar(),
                    () -> zonaService.actualizar(zona.getIdZona(), datos),
                    cambiada -> cargar(cambiada.getIdZona()),
                    mensaje -> {
                        vista.mostrarError(mensaje);
                        cargar(null);
                    });
        });
    }

    public void eliminar() {
        vista.seleccionado().ifPresent(zona -> {
            if (!vista.confirmar("¿Eliminar la zona \"" + zona.getNombre() + "\"?")) {
                return;
            }
            Tarea.ejecutar(vista.getBotonEliminar(), () -> {
                try {
                    zonaService.eliminar(zona.getIdZona());
                    return true;
                } catch (DataIntegrityViolationException e) {
                    throw new IllegalStateException("La zona \"" + zona.getNombre() + "\" tiene reservas registradas "
                            + "y no se puede eliminar. Desactivala para que no se use más.");
                }
            }, ok -> cargar(null));
        });
    }

    private void abrir(Zona original) {
        ZonaForm form = new ZonaForm(original);
        FormularioDialog.abrir(vista, original == null ? "Nueva zona" : "Modificar zona", form,
                () -> preparar(form, original)
        ).ifPresent(guardada -> cargar(guardada.getIdZona()));
    }

    /** Lee y valida el formulario (en el hilo de Swing) y devuelve el trabajo que guarda. */
    private Callable<Zona> preparar(ZonaForm form, Zona original) {
        String nombre = form.getNombre();
        Integer capacidad = form.getCapacidad();
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("Completá el nombre de la zona.");
        }
        if (capacidad == null || capacidad < 1) {
            throw new IllegalArgumentException("La capacidad tiene que ser un número mayor a 0.");
        }
        boolean repetido = zonas.stream().anyMatch(z -> z.getNombre() != null
                && z.getNombre().equalsIgnoreCase(nombre)
                && (original == null || z.getIdZona() != original.getIdZona()));
        if (repetido) {
            throw new IllegalArgumentException("Ya hay una zona llamada \"" + nombre + "\".");
        }

        Zona datos = new Zona();
        datos.setNombre(nombre);
        datos.setCapacidadMaxima(capacidad);
        datos.setActiva(form.isActiva());
        int restaurante = Sesion.nroRestaurante();

        return () -> {
            if (original != null) {
                return zonaService.actualizar(original.getIdZona(), datos);
            }
            datos.setRestaurante(restauranteService.buscarPorId(restaurante));
            return zonaService.guardar(datos);
        };
    }
}