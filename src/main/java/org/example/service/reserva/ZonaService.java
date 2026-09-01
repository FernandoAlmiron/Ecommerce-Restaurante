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
                .orElseThrow(() -> new RuntimeException("Zona no encontrada"));
    }

    public List<Zona> listarTodos() {
        return zonaRepository.findAll();
    }
}