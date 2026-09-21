package org.example.service.reserva;

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
        return zonaRepository.save(zona);
    }

    public Zona buscarPorId(int id) {
        return zonaRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Zona no encontrada"));
    }

    public List<Zona> listarTodos() {
        return zonaRepository.findAll();
    }

    public Zona actualizar(int idZona, Zona datos) {
        Zona actual = buscarPorId(idZona);
        actual.setNombre(datos.getNombre());
        actual.setCapacidadMaxima(datos.getCapacidadMaxima());
        actual.setActiva(datos.isActiva());
        return zonaRepository.save(actual);
    }

    public void eliminar(int idZona) {
        buscarPorId(idZona);   // 404 si no existe
        zonaRepository.deleteById(idZona);   // si tiene datos asociados la base lo rechaza (409)
    }
}
