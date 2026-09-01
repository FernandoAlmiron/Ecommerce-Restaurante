package org.example.service.facturacion;

import org.example.Modelo.facturacion.Facturacion;
import org.example.repository.facturacion.FacturacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacturacionService {

    private final FacturacionRepository facturacionRepository;

    public FacturacionService(FacturacionRepository facturacionRepository) {
        this.facturacionRepository = facturacionRepository;
    }

    public Facturacion guardar(Facturacion facturacion) {
        return facturacionRepository.save(facturacion);
    }

    public Facturacion buscarPorId(int nroFacturacion) {
        return facturacionRepository.findById(nroFacturacion)
                .orElseThrow(() -> new RuntimeException("Facturacion no encontrada"));
    }

    public List<Facturacion> listarTodos() {
        return facturacionRepository.findAll();
    }

    public double calcularTotal(int nroFacturacion) {
        Facturacion facturacion = buscarPorId(nroFacturacion);
        double total = facturacion.calcularTotal();
        facturacionRepository.save(facturacion);
        return total;
    }

    public void imprimirTicket(int nroFacturacion) {
        Facturacion facturacion = buscarPorId(nroFacturacion);
        facturacion.imprimirTicket();
    }
}
