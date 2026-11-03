package org.example.service.persona;

import org.example.Modelo.Restaurante;
import org.example.Modelo.persona.Cliente;
import org.example.Modelo.persona.Persona;
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
        if (cliente.getDni() < 1_000_000 || cliente.getDni() > 99_999_999) {
            throw new IllegalArgumentException("El DNI tiene que tener 7 u 8 números, sin puntos");
        }
        cliente.setCelular(Persona.normalizarCelular(cliente.getCelular()));
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
            nuevo.setCelular(Persona.normalizarCelular(celular));
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
        actual.setCelular(Persona.normalizarCelular(datos.getCelular()));
        actual.setDni(datos.getDni());
        actual.setEmail(datos.getEmail());
        return clienteRepository.save(actual);
    }

    /**
     * El cliente cambia sus propios datos desde el perfil de la web. El DNI no se cambia: con él se lo
     * reconoce en las reservas y los pedidos. Si entra con su email (usuario = email), el usuario cambia
     * junto con el email.
     * @return el cliente actualizado (si cambió el usuario, tiene que volver a iniciar sesión)
     */
    @org.springframework.transaction.annotation.Transactional
    public Cliente actualizarPerfil(String username, String nombre, String apellido, String email, String celular) {
        Cliente actual = buscarPorUsername(username);
        String nom = validarNombre(nombre, "nombre");
        String ape = validarNombre(apellido, "apellido");
        String mail = email == null ? "" : email.trim().toLowerCase();
        if (!mail.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("El email no es válido");
        }
        String cel = Persona.normalizarCelular(celular);

        boolean entraConEmail = actual.getUsername() != null && actual.getUsername().equalsIgnoreCase(actual.getEmail());
        if (entraConEmail && !mail.equalsIgnoreCase(actual.getUsername())) {
            // el email nuevo va a ser su usuario: no puede ser el de otra cuenta
            boolean ocupado = clienteRepository.findByUsername(mail).filter(c -> c.getIdCliente() != actual.getIdCliente()).isPresent()
                    || empleadoRepository.findByUsername(mail) != null;
            if (ocupado) {
                throw new IllegalStateException("Ese email ya tiene una cuenta");
            }
            actual.setUsername(mail);
        }
        actual.setNombre(nom);
        actual.setApellido(ape);
        actual.setEmail(mail);
        actual.setCelular(cel);
        return clienteRepository.save(actual);
    }

    /** El cliente cambia su contraseña: tiene que poner la actual. La nueva, de al menos 6 caracteres. */
    @org.springframework.transaction.annotation.Transactional
    public void cambiarClave(String username, String claveActual, String claveNueva) {
        Cliente actual = buscarPorUsername(username);
        if (claveActual == null || actual.getPassword() == null || !passwordEncoder.matches(claveActual, actual.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }
        if (claveNueva == null || claveNueva.length() < 6) {
            throw new IllegalArgumentException("La contraseña nueva debe tener al menos 6 caracteres");
        }
        actual.setPassword(passwordEncoder.encode(claveNueva));
        clienteRepository.save(actual);
    }

    /** Nombre o apellido: obligatorio y solo letras (con tilde y ñ), espacios, ' . y - */
    private static String validarNombre(String texto, String campo) {
        String limpio = texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("El " + campo + " es obligatorio");
        }
        if (!limpio.matches("\\p{L}[\\p{L}\\s'.-]*")) {
            throw new IllegalArgumentException("El " + campo + " solo puede tener letras");
        }
        return limpio;
    }

    public void eliminar(int idCliente) {
        buscarPorId(idCliente);   // 404 si no existe
        clienteRepository.deleteById(idCliente);   // si tiene datos asociados la base lo rechaza (409)
    }
}
