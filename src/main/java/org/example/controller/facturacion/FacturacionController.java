package org.example.controller.facturacion;

import org.example.Modelo.facturacion.Facturacion;
import org.example.service.facturacion.FacturacionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}