package org.example.controller.reserva;

import org.example.Modelo.reserva.Envio;
import org.example.Modelo.enums.EstadoEnvio;
import org.example.service.reserva.EnvioService;
import org.example.dto.EnvioDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/envios")
public class EnvioController {
    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) { this.envioService = envioService; }

    @PostMapping
    public Envio crear(@RequestBody EnvioDTO dto) { return envioService.crearEnvioConPedido(dto); }

    @GetMapping
    public List<Envio> listar() { return envioService.listarTodos(); }

    @GetMapping("/{id}")
    public Envio buscar(@PathVariable int id) { return envioService.buscarPorId(id); }

    @PutMapping("/{id}/asignar-repartidor/{legajo}")
    public Envio asignar(@PathVariable int id, @PathVariable int legajo) {
        return envioService.asignarRepartidor(id, legajo);
    }

    @PutMapping("/{id}/estado")
    public Envio cambiarEstado(@PathVariable int id, @RequestParam EstadoEnvio estado) {
        return envioService.actualizarEstado(id, estado);
    }
}
