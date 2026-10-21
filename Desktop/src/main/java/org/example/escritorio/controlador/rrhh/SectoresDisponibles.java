package org.example.escritorio.controlador.rrhh;

import org.example.Modelo.persona.Sector;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.service.persona.SectorService;

import java.util.Comparator;
import java.util.List;

/**
 * Los sectores que se le pueden asignar a un empleado: activos, del restaurante del empleado logueado
 * y sin ADMINISTRACION si quien usa la pantalla no es ADMIN (RF-55; el backend también lo controla).
 * Lo usan el alta y el cambio de sector. Llamarlo dentro de una Tarea (consulta la base).
 */
final class SectoresDisponibles {

    private SectoresDisponibles() {
    }

    static List<Sector> listar() {
        int restaurante = Sesion.nroRestaurante();
        boolean admin = Sesion.esAdmin();
        return Contexto.get(SectorService.class).listarTodos().stream()
                .filter(Sector::isActiva)
                .filter(s -> s.getRestaurante() == null
                        || s.getRestaurante().getNroRestaurante() == restaurante)
                .filter(s -> admin || !"ADMINISTRACION".equalsIgnoreCase(s.getNombre()))
                .sorted(Comparator.comparing(Sector::getNombre))
                .toList();
    }
}