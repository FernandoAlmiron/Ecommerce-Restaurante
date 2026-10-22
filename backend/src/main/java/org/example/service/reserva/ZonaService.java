package org.example.service.reserva;

import org.example.Modelo.Restaurante;
import org.example.Modelo.reserva.Zona;
import org.example.repository.reserva.ZonaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZonaService {

    private final ZonaRepository zonaRepository;

    public ZonaService(ZonaRepository zonaRepository) {
        this.zonaRepository = zonaRepository;
    }

    public Zona guardar(Zona zona) {
        validar(zona.getNombre(), zona.getCapacidadMaxima(), zona.getRestaurante(), zona.getIdZona());
        zona.setNombre(zona.getNombre().trim());
        return zonaRepository.save(zona);
    }

    public Zona buscarPorId(int id) {
        return zonaRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Zona no encontrada"));
    }

    public List<Zona> listarTodos() {
        return zonaRepository.findAll();
    }

    /** Las zonas habilitadas de un restaurante: las que se ofrecen para reservar o sentar clientes. */
    public List<Zona> listarActivas(int nroRestaurante) {
        return zonaRepository.findByRestaurante_NroRestauranteAndActivaTrueOrderByNombre(nroRestaurante);
    }

    public Zona actualizar(int idZona, Zona datos) {
        Zona actual = buscarPorId(idZona);
        validar(datos.getNombre(), datos.getCapacidadMaxima(), actual.getRestaurante(), idZona);
        actual.setNombre(datos.getNombre().trim());
        actual.setCapacidadMaxima(datos.getCapacidadMaxima());
        actual.setActiva(datos.isActiva());
        return zonaRepository.save(actual);
    }

    /** Nombre obligatorio y sin repetir en el restaurante; capacidad mayor a 0. */
    private void validar(String nombre, int capacidad, Restaurante restaurante, int idPropio) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la zona es obligatorio");
        }
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad de la zona tiene que ser mayor a 0");
        }
        if (restaurante != null && zonaRepository.existsByNombreIgnoreCaseAndRestaurante_NroRestauranteAndIdZonaNot(
                nombre.trim(), restaurante.getNroRestaurante(), idPropio)) {
            throw new IllegalStateException("Ya hay una zona llamada " + nombre.trim());
        }
    }

    public void eliminar(int idZona) {
        buscarPorId(idZona);   // 404 si no existe
        zonaRepository.deleteById(idZona);   // si tiene datos asociados la base lo rechaza (409)
    }
}
