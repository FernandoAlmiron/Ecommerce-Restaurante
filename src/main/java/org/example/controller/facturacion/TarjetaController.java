package org.example.controller.facturacion;

import org.example.Modelo.reserva.Tarjeta;
import org.example.service.reserva.TarjetaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tarjetas")
public class TarjetaController {

    private final TarjetaService tarjetaService;

    public TarjetaController(TarjetaService tarjetaService) {
        this.tarjetaService = tarjetaService;
    }

    @GetMapping
    public List<Tarjeta> listarTodos() {
        return tarjetaService.listarTodos();
    }

    @GetMapping("/{id}")
    public Tarjeta buscarPorId(@PathVariable int id) {
        return tarjetaService.buscarPorId(id);
    }

    @PostMapping
    public Tarjeta crear(@RequestBody Tarjeta tarjeta) {
        tarjeta.setNroTarjeta(0);
        return tarjetaService.guardar(tarjeta);
    }
}