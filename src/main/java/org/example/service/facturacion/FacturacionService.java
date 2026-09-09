package org.example.service.facturacion;

import org.example.Modelo.facturacion.Facturacion;
import org.example.repository.facturacion.FacturacionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public Facturacion aplicarPropina(int nroFacturacion, int porcentaje) {
        Facturacion facturacion = facturacionRepository.findById(nroFacturacion).orElseThrow();
        facturacion.aplicarPropina(porcentaje);
        return facturacionRepository.save(facturacion);
    }

    public Facturacion confirmarPago(int nroFacturacion) {
        Facturacion f = facturacionRepository.findById(nroFacturacion).orElseThrow();
        f.confirmarPago();
        return facturacionRepository.save(f);
    }

    public Map<String, Double> reporteDelivery(int nroRestaurante, LocalDate fecha) {
        List<Object[]> filas = facturacionRepository.resumenDeliveryPorMetodoPago(nroRestaurante, fecha);
        Map<String, Double> resultado = new LinkedHashMap<>();
        for (Object[] fila : filas) {
            resultado.put(fila[0].toString(), (Double) fila[1]);
        }
        return resultado;
    }
}
