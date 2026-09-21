package org.example.service.reserva;

import org.example.Modelo.reserva.Tarjeta;
import org.example.repository.reserva.TarjetaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TarjetaService {

    private final TarjetaRepository tarjetaRepository;

    public TarjetaService(TarjetaRepository tarjetaRepository) {
        this.tarjetaRepository = tarjetaRepository;
    }

    public Tarjeta guardar(Tarjeta tarjeta) {
        return tarjetaRepository.save(tarjeta);
    }

    public Tarjeta buscarPorId(int nroTarjeta) {
        return tarjetaRepository.findById(nroTarjeta)
                .orElseThrow(() -> new java.util.NoSuchElementException("Tarjeta no encontrada"));
    }

    public List<Tarjeta> listarTodos() {
        return tarjetaRepository.findAll();
    }

    public boolean esValida(int nroTarjeta) {
        Tarjeta tarjeta = buscarPorId(nroTarjeta);
        return tarjeta.esValida();
    }
}
