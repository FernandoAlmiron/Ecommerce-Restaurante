package org.example.escritorio.controlador.rrhh;

import org.example.Modelo.persona.Empleado;
import org.example.Modelo.persona.Sector;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.comun.Tarea;
import org.example.escritorio.vista.rrhh.EmpleadoDialog;
import org.example.service.persona.EmpleadoService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

import java.util.Arrays;

/**
 * Guarda un empleado nuevo (RF-53) o sus datos personales (RF-54) con EmpleadoService.
 * El backend exige usuario, clave y sector, guarda la clave con BCrypt y protege ADMINISTRACION.
 */
public class EmpleadoEdicionControlador {

    private final EmpleadoDialog vista;
    private final EmpleadoService empleadoService;

    public EmpleadoEdicionControlador(EmpleadoDialog vista) {
        this.vista = vista;
        this.empleadoService = Contexto.get(EmpleadoService.class);
    }

    /** Solo para el alta: los sectores que se pueden asignar. */
    public void cargarSectores() {
        Tarea.ejecutar(vista.getComboSector(), SectoresDisponibles::listar, vista::mostrarSectores);
    }

    public void guardar() {
        if (vista.getNombre().isEmpty() || vista.getApellido().isEmpty()) {
            vista.mostrarError("Completá nombre y apellido.");
            return;
        }
        if (!vista.getDniTexto().matches("\\d{7,8}")) {
            vista.mostrarError("El DNI tiene que tener 7 u 8 números, sin puntos.");
            return;
        }
        if (vista.getUsuario().isEmpty()) {
            vista.mostrarError("Completá el usuario.");
            return;
        }

        Empleado datos = new Empleado();
        datos.setNombre(vista.getNombre());
        datos.setApellido(vista.getApellido());
        datos.setDni(Integer.parseInt(vista.getDniTexto()));
        datos.setCelular(vista.getCelular().isEmpty() ? null : vista.getCelular());
        datos.setUsername(vista.getUsuario());

        Empleado original = vista.getEmpleado();
        boolean admin = Sesion.esAdmin();
        if (original == null && !completarAlta(datos)) {
            return;
        }

        Tarea.ejecutar(vista.getBotonGuardar(), () -> {
            try {
                return original == null
                        ? empleadoService.guardar(datos, admin)
                        : empleadoService.actualizar(original.getLegajo(), datos, admin);
            } catch (AccessDeniedException e) {
                throw new IllegalStateException("Solo un administrador puede cargar o modificar empleados de ADMINISTRACION.");
            } catch (DataIntegrityViolationException e) {
                throw new IllegalStateException("Ya existe un empleado con el usuario \"" + datos.getUsername() + "\".");
            }
        }, vista::cerrarCon);
    }

    /** En el alta se agregan la clave (repetida para evitar errores de tipeo) y el sector. */
    private boolean completarAlta(Empleado datos) {
        char[] clave = vista.getClave();
        char[] repetida = vista.getRepetirClave();
        try {
            if (clave.length == 0) {
                vista.mostrarError("Completá la clave.");
                return false;
            }
            if (!Arrays.equals(clave, repetida)) {
                vista.mostrarError("Las dos claves no coinciden.");
                return false;
            }
            Sector sector = vista.getSector();
            if (sector == null) {
                vista.mostrarError("Elegí el sector.");
                return false;
            }
            datos.setPassword(new String(clave)); // el backend la guarda con BCrypt
            datos.setSector(sector);
            return true;
        } finally {
            Arrays.fill(clave, '\0');
            Arrays.fill(repetida, '\0');
        }
    }
}