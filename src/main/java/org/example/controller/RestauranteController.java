package org.example.controller;

import org.example.Modelo.Restaurante;
import org.example.service.RestauranteService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/restaurantes")
public class RestauranteController {

    private final RestauranteService restauranteService;

    public RestauranteController(RestauranteService restauranteService) {
        this.restauranteService = restauranteService;
    }

    @GetMapping
    public List<Restaurante> listarTodos() {
        return restauranteService.listarTodos();
    }

    @GetMapping("/{id}")
    public Restaurante buscarPorId(@PathVariable int id) {
        return restauranteService.buscarPorId(id);
    }

    @PostMapping
    public Restaurante crear(@RequestBody Restaurante restaurante) {
        restaurante.setNroRestaurante(0);
        return restauranteService.guardar(restaurante);
    }

    @GetMapping("/{id}/abierto")
    public boolean estaAbierto(@PathVariable int id, @RequestParam LocalTime hora) {
        return restauranteService.estaAbierto(id, hora);
    }
}
