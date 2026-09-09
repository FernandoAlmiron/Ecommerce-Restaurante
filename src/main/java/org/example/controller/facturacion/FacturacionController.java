package org.example.controller.facturacion;

import org.example.Modelo.facturacion.Facturacion;
import org.example.service.facturacion.FacturacionService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturaciones")
public class FacturacionController {

    private final FacturacionService facturacionService;

    public FacturacionController(FacturacionService facturacionService) {
        this.facturacionService = facturacionService;
    }

    @GetMapping
    public List<Facturacion> listarTodos() {
        return facturacionService.listarTodos();
    }

    @GetMapping("/{id}")
    public Facturacion buscarPorId(@PathVariable int id) {
        return facturacionService.buscarPorId(id);
    }

    @PostMapping
    public Facturacion crear(@RequestBody Facturacion facturacion) {
        return facturacionService.guardar(facturacion);
    }

    @PutMapping("/{id}/calcular-total")
    public double calcularTotal(@PathVariable int id) {
        return facturacionService.calcularTotal(id);
    }

    @PutMapping("/{id}/propina")
    public Facturacion aplicarPropina(@PathVariable int id, @RequestParam int porcentaje) {
        return facturacionService.aplicarPropina(id, porcentaje);
    }

    @PutMapping("/{id}/confirmar-pago")
    public Facturacion confirmarPago(@PathVariable int id) {
        return facturacionService.confirmarPago(id);
    }

    @GetMapping("/reporte-delivery")
    public Map<String, Double> reporteDelivery(@RequestParam int nroRestaurante,
                                               @RequestParam(required = false) String fecha) {
        LocalDate f = (fecha != null) ? LocalDate.parse(fecha) : LocalDate.now();
        return facturacionService.reporteDelivery(nroRestaurante, f);
    }
}