package org.example.service.reserva;

import org.example.Modelo.reserva.Senia;
import org.example.repository.reserva.SeniaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeniaService {

    private final SeniaRepository seniaRepository;

    public SeniaService(SeniaRepository seniaRepository) {
        this.seniaRepository = seniaRepository;
    }

    public Senia guardar(Senia senia) {
        return seniaRepository.save(senia);
    }

    public Senia buscarPorId(int idSenia) {
        return seniaRepository.findById(idSenia)
                .orElseThrow(() -> new RuntimeException("Senia no encontrada"));
    }

    public List<Senia> listarTodos() {
        return seniaRepository.findAll();
    }

    public Senia cobrarPorInasistencia(int idSenia) {
        Senia senia = buscarPorId(idSenia);
        senia.cobraPorInasistencia();
        return seniaRepository.save(senia);
    }

}
