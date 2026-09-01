package org.example.controller.reserva;

import org.example.Modelo.reserva.Senia;
import org.example.service.reserva.SeniaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/senias")
public class SeniaController {

    private final SeniaService seniaService;

    public SeniaController(SeniaService seniaService) {
        this.seniaService = seniaService;
    }

    @GetMapping
    public List<Senia> listarTodos() {
        return seniaService.listarTodos();
    }

    @GetMapping("/{id}")
    public Senia buscarPorId(@PathVariable int id) {
        return seniaService.buscarPorId(id);
    }

    @PostMapping
    public Senia crear(@RequestBody Senia senia) {
        return seniaService.guardar(senia);
    }
}
