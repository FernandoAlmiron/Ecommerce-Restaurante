package org.example.controller.reserva;

import org.example.Modelo.reserva.Envio;
import org.example.Modelo.enums.EstadoEnvio;
import org.example.service.reserva.EnvioService;
import org.example.dto.EnvioDTO;
import org.example.dto.RemitoDTO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/envios")
public class EnvioController {
    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) { this.envioService = envioService; }

    @PostMapping
    public Envio crear(@RequestBody EnvioDTO dto, Authentication authentication) {
        return envioService.crearEnvioConPedido(dto, clienteLogueado(authentication));
    }

    @GetMapping
    public List<Envio> listar() { return envioService.listarTodos(); }

    @GetMapping("/{id}")
    public Envio buscar(@PathVariable int id) { return envioService.buscarPorId(id); }

    // ticket de envio: lo que se entrega al cliente, con sus datos
    @GetMapping("/{id}/remito")
    public RemitoDTO remito(@PathVariable int id) {
        return RemitoDTO.desde(envioService.buscarPorId(id));
    }

    @PutMapping("/{id}/asignar-repartidor/{legajo}")
    public Envio asignar(@PathVariable int id, @PathVariable int legajo) {
        return envioService.asignarRepartidor(id, legajo);
    }

    @PutMapping("/{id}/estado")
    public Envio cambiarEstado(@PathVariable int id, @RequestParam EstadoEnvio estado,
                               Authentication authentication) {
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return envioService.actualizarEstado(id, estado, authentication.getName(), esAdmin);
    }

    // usuario del cliente logueado, o null si pide sin cuenta
    private String clienteLogueado(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE")) ? auth.getName() : null;
    }
}
