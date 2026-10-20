package org.example.service.persona;

import org.example.Modelo.Restaurante;
import org.example.Modelo.persona.Cliente;
import org.example.repository.persona.ClienteRepository;
import org.example.repository.persona.EmpleadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, EmpleadoRepository empleadoRepository,
                          PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Registro publico. Con usuario y clave queda una cuenta para pedir sin repetir datos.
    // Registro publico. Con usuario y clave queda una cuenta para pedir sin repetir datos.
    public Cliente guardar(Cliente cliente) {
        if (cliente.getNombre() == null || cliente.getNombre().isBlank()
                || cliente.getApellido() == null || cliente.getApellido().isBlank() || cliente.getDni() <= 0) {
            throw new IllegalArgumentException("Nombre, apellido y DNI son obligatorios");
        }
        String usuario = cliente.getUsername();
        if (usuario == null || usuario.isBlank()) {
            cliente.setUsername(null);
            cliente.setPassword(null);
            return clienteRepository.save(cliente);
        }
        if (cliente.getPassword() == null || cliente.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        if (clienteRepository.findByUsername(usuario).isPresent() || empleadoRepository.findByUsername(usuario) != null) {
            throw new IllegalStateException("Ese nombre de usuario ya existe");
        }
        // si ya habia comprado sin cuenta, se le agrega la cuenta a su registro en lugar de duplicarlo
        Optional<Cliente> existente = clienteRepository.findFirstByDni(cliente.getDni());
        if (existente.isPresent() && existente.get().getUsername() != null) {
            throw new IllegalStateException("Ese DNI ya tiene una cuenta");
        }
        Cliente destino = existente.orElse(cliente);
        destino.setNombre(cliente.getNombre());
        destino.setApellido(cliente.getApellido());
        destino.setCelular(cliente.getCelular());
        destino.setEmail(cliente.getEmail());
        destino.setRestaurante(cliente.getRestaurante());
        destino.setUsername(usuario);
        destino.setPassword(passwordEncoder.encode(cliente.getPassword()));
        return clienteRepository.save(destino);
    }

    // el cliente logueado, a partir del usuario que viene en el request
    public Cliente buscarPorUsername(String username) {
        return clienteRepository.findByUsername(username)
                .orElseThrow(() -> new java.util.NoSuchElementException("Cliente no encontrado"));
    }

    // El cliente no conoce su id: se lo busca por DNI y, si es la primera vez, se lo registra
    public Cliente obtenerOCrear(int dni, String nombre, String apellido, String celular,
                                 String email, Restaurante restaurante) {
        if (dni <= 0) {
            throw new IllegalArgumentException("El DNI del cliente es obligatorio");
        }
        return clienteRepository.findFirstByDni(dni).orElseGet(() -> {
            if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()) {
                throw new IllegalArgumentException("Cliente nuevo: nombre y apellido son obligatorios");
            }
            Cliente nuevo = new Cliente();
            nuevo.setNombre(nombre);
            nuevo.setApellido(apellido);
            nuevo.setCelular(celular);
            nuevo.setEmail(email);
            nuevo.setDni(dni);
            nuevo.setRestaurante(restaurante);
            return clienteRepository.save(nuevo);
        });
    }

    public Cliente buscarPorId(int idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new java.util.NoSuchElementException("Cliente no encontrado"));
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente actualizar(int idCliente, Cliente datos) {
        Cliente actual = buscarPorId(idCliente);
        actual.setNombre(datos.getNombre());
        actual.setApellido(datos.getApellido());
        actual.setCelular(datos.getCelular());
        actual.setDni(datos.getDni());
        actual.setEmail(datos.getEmail());
        return clienteRepository.save(actual);
    }

    public void eliminar(int idCliente) {
        buscarPorId(idCliente);   // 404 si no existe
        clienteRepository.deleteById(idCliente);   // si tiene datos asociados la base lo rechaza (409)
    }
}
