package org.example.controller.persona;

import org.example.Modelo.persona.Cliente;
import org.example.dto.RemitoDTO;
import org.example.dto.ReservaResponseDTO;
import org.example.service.persona.ClienteService;
import org.example.service.reserva.EnvioService;
import org.example.service.reserva.ReservaService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Lo que ve un cliente con cuenta: sus datos, sus reservas y sus envios. Nunca los de otro.
@RestController
@RequestMapping("/api/mi-cuenta")
public class MiCuentaController {

    private final ClienteService clienteService;
    private final ReservaService reservaService;
    private final EnvioService envioService;

    public MiCuentaController(ClienteService clienteService, ReservaService reservaService,
                              EnvioService envioService) {
        this.clienteService = clienteService;
        this.reservaService = reservaService;
        this.envioService = envioService;
    }

    @GetMapping
    public Cliente misDatos(Authentication authentication) {
        return clienteService.buscarPorUsername(authentication.getName());
    }

    @GetMapping("/reservas")
    public List<ReservaResponseDTO> misReservas(Authentication authentication) {
        Cliente cliente = clienteService.buscarPorUsername(authentication.getName());
        return reservaService.listarPorCliente(cliente.getIdCliente()).stream()
                .map(ReservaResponseDTO::desde).toList();
    }

    @GetMapping("/envios")
    public List<RemitoDTO> misEnvios(Authentication authentication) {
        Cliente cliente = clienteService.buscarPorUsername(authentication.getName());
        return envioService.listarPorCliente(cliente.getIdCliente()).stream()
                .map(RemitoDTO::desde).toList();
    }
}
