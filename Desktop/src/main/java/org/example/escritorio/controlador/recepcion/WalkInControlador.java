package org.example.escritorio.controlador.recepcion;

import org.example.Modelo.persona.Cliente;
import org.example.Modelo.reserva.Zona;
import org.example.dto.ReservaResponseDTO;
import org.example.dto.ReservaWalkInDTO;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.comun.Tarea;
import org.example.escritorio.vista.recepcion.WalkInDialog;
import org.example.repository.persona.ClienteRepository;
import org.example.service.reserva.ReservaService;
import org.example.service.reserva.ZonaService;

/**
 * Lógica del walk-in (RF-17). Fecha, hora, estado CONFIRMADA y control de horario y cupo
 * los pone ReservaService.reservarWalkIn del backend, no la pantalla.
 */
public class WalkInControlador {

    private final WalkInDialog vista;
    private final ReservaService reservaService;
    private final ZonaService zonaService;
    private final ClienteRepository clienteRepository;

    /** true cuando ya se buscó el DNI escrito; si cambia el DNI hay que volver a buscar. */
    private boolean dniBuscado;
    /** El cliente encontrado, o null si es nuevo. */
    private Cliente clienteExistente;

    public WalkInControlador(WalkInDialog vista) {
        this.vista = vista;
        this.reservaService = Contexto.get(ReservaService.class);
        this.zonaService = Contexto.get(ZonaService.class);
        this.clienteRepository = Contexto.get(ClienteRepository.class);
    }

    /** Solo las zonas activas del restaurante del empleado. */
    public void cargarZonas() {
        int restaurante = Sesion.nroRestaurante();
        Tarea.ejecutar(vista.getComboZona(),
                () -> zonaService.listarTodos().stream()
                        .filter(Zona::isActiva)
                        .filter(z -> z.getRestaurante() != null
                                && z.getRestaurante().getNroRestaurante() == restaurante)
                        .toList(),
                vista::mostrarZonas);
    }

    public void buscarCliente() {
        Integer dni = leerDni();
        if (dni == null) {
            vista.mostrarError("El DNI tiene que tener 7 u 8 números, sin puntos.");
            return;
        }
        Tarea.ejecutar(vista.getBotonBuscar(), () -> clienteRepository.findFirstByDni(dni), encontrado -> {
            dniBuscado = true;
            clienteExistente = encontrado.orElse(null);
            if (clienteExistente != null) {
                vista.mostrarClienteExistente(clienteExistente);
            } else {
                vista.prepararClienteNuevo();
            }
            vista.habilitarRegistrar(true);
        });
    }

    public void alCambiarDni() {
        if (dniBuscado) {
            dniBuscado = false;
            clienteExistente = null;
            vista.limpiarCliente();
        }
    }

    public void registrar() {
        if (!dniBuscado) {
            vista.mostrarError("Primero buscá el cliente por DNI.");
            return;
        }
        Zona zona = vista.getZonaSeleccionada();
        if (zona == null) {
            vista.mostrarError("Elegí una zona.");
            return;
        }
        if (clienteExistente == null && (vista.getNombre().isEmpty() || vista.getApellido().isEmpty())) {
            vista.mostrarError("Completá nombre y apellido del cliente.");
            return;
        }

        ReservaWalkInDTO datos = new ReservaWalkInDTO();
        datos.setNroRestaurante(Sesion.nroRestaurante());
        datos.setIdZona(zona.getIdZona());
        datos.setCantComensales(vista.getComensales());
        datos.setDni(leerDni());
        if (clienteExistente != null) {
            datos.setIdCliente(clienteExistente.getIdCliente());
        } else {
            datos.setNombre(vista.getNombre());
            datos.setApellido(vista.getApellido());
            datos.setCelular(vista.getCelular().isEmpty() ? null : vista.getCelular());
        }

        Tarea.ejecutar(vista.getBotonRegistrar(),
                () -> ReservaResponseDTO.desde(reservaService.reservarWalkIn(datos)),
                vista::cerrarCon);
    }

    /** Valida solo el formato; si el cliente existe o no lo resuelve el backend. */
    private Integer leerDni() {
        String texto = vista.getDniTexto();
        if (!texto.matches("\\d{7,8}")) {
            return null;
        }
        return Integer.valueOf(texto);
    }
}