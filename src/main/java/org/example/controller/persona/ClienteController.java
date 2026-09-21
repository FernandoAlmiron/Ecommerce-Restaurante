package org.example.controller.persona;

import org.example.Modelo.persona.Cliente;
import org.example.service.persona.ClienteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<Cliente> listarTodos() {
        return clienteService.listarTodos();
    }

    @GetMapping("/{id}")
    public Cliente buscarPorId(@PathVariable int id) {
        return clienteService.buscarPorId(id);
    }

    @PostMapping
    public Cliente crear(@RequestBody Cliente cliente) {
        cliente.setIdCliente(0);
        return clienteService.guardar(cliente);
    }
}
