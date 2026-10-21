package org.example.escritorio.controlador.admin;

import org.example.Modelo.Restaurante;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.admin.RestaurantePanel;
import org.example.escritorio.vista.comun.Tarea;
import org.example.service.RestauranteService;

/**
 * Datos del restaurante del usuario logueado con RestauranteService (RF-49).
 * El backend exige nombre y los dos horarios.
 */
public class RestauranteControlador {

    private final RestaurantePanel vista;
    private final RestauranteService restauranteService;

    public RestauranteControlador(RestaurantePanel vista) {
        this.vista = vista;
        this.restauranteService = Contexto.get(RestauranteService.class);
    }

    /** Trae los datos guardados (también sirve para deshacer lo que se escribió). */
    public void cargar() {
        int nro = Sesion.nroRestaurante();
        Tarea.ejecutar(vista.getBotonDeshacer(), () -> restauranteService.buscarPorId(nro), vista::mostrar);
    }

    public void guardar() {
        if (vista.getApertura() == null || vista.getCierre() == null) {
            vista.mostrarError("Revisá el horario: escribilo como 19:30.");
            return;
        }
        if (vista.getApertura().equals(vista.getCierre())) {
            vista.mostrarError("La hora de apertura y la de cierre no pueden ser iguales.");
            return;
        }
        String email = vista.getEmail();
        if (!email.isEmpty() && !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            vista.mostrarError("El email no parece válido.");
            return;
        }

        Restaurante datos = new Restaurante();
        datos.setNombre(vista.getNombre()); // si está vacío lo rechaza RestauranteService
        datos.setDireccion(vacioANull(vista.getDireccion()));
        datos.setTelefono(vacioANull(vista.getTelefono()));
        datos.setEmail(vacioANull(email));
        datos.setHorarioApertura(vista.getApertura());
        datos.setHorarioCierre(vista.getCierre());
        int nro = Sesion.nroRestaurante();

        Tarea.ejecutar(vista.getBotonGuardar(), () -> restauranteService.actualizar(nro, datos), guardado -> {
            vista.mostrar(guardado);
            vista.mostrarInfo("Se guardaron los datos del restaurante.");
        });
    }

    private static String vacioANull(String texto) {
        return texto.isEmpty() ? null : texto;
    }
}