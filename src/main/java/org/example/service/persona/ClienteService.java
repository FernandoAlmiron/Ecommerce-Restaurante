package org.example.service.persona;

import org.example.Modelo.persona.Cliente;
import org.example.repository.persona.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente guardar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public Cliente buscarPorId(int idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new java.util.NoSuchElementException("Cliente no encontrado"));
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }
}
