package org.example.config;

import org.example.Modelo.persona.Cliente;
import org.example.repository.persona.ClienteRepository;
import org.example.Modelo.persona.Empleado;
import org.example.repository.persona.EmpleadoRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmpleadoRepository empleadoRepository;
    private final ClienteRepository clienteRepository;

    public CustomUserDetailsService(EmpleadoRepository empleadoRepository, ClienteRepository clienteRepository) {
        this.empleadoRepository = empleadoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Empleado empleado = empleadoRepository.findByUsername(username);
        if (empleado == null) {
            // si no es empleado, puede ser un cliente con cuenta
            Cliente cliente = clienteRepository.findByUsername(username)
                    .filter(c -> c.getPassword() != null)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
            return new User(cliente.getUsername(), cliente.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
        }

        String rol = mapearRol(empleado.getSector().getNombre());

        return new User(empleado.getUsername(), empleado.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
    }

    private String mapearRol(String sector) {
        return switch (sector.toUpperCase()) {
            case "ADMINISTRACION" -> "ADMIN";
            case "COCINA" -> "COCINA";
            case "SALON", "BARRA" -> "MOZO";
            case "CAJA" -> "CAJA";
            case "REPARTIDOR" -> "REPARTIDOR";
            case "RECEPCION" -> "RECEPCION";
            case "RECURSOSHUMANOS" -> "RRHH";
            default -> "EMPLEADO";
        };
    }
}
