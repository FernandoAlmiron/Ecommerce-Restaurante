package org.example.config;

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

    public CustomUserDetailsService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Empleado empleado = empleadoRepository.findByUsername(username);
        if (empleado == null) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
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
            default -> "EMPLEADO";
        };
    }
}
