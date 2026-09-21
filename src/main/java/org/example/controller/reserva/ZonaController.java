package org.example.controller.reserva;

import org.example.Modelo.reserva.Zona;
import org.example.service.reserva.ZonaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zonas")
public class ZonaController {

    private final ZonaService zonaService;

    public ZonaController(ZonaService zonaService) {
        this.zonaService = zonaService;
    }

    @GetMapping
    public List<Zona> listarTodos() {
        return zonaService.listarTodos();
    }

    @GetMapping("/{id}")
    public Zona buscarPorId(@PathVariable int id) {
        return zonaService.buscarPorId(id);
    }

    @PostMapping
    public Zona crear(@RequestBody Zona zona) {
        zona.setIdZona(0);
        return zonaService.guardar(zona);
    }
}
