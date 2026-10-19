package org.example.escritorio.sesion;

import org.example.Modelo.persona.Empleado;
import org.example.repository.persona.EmpleadoRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;

/**
 * Login de la app de escritorio. Usa lo mismo que la API:
 * el CustomUserDetailsService del backend (busca el usuario y le asigna el rol según su sector)
 * y BCrypt para comparar la clave.
 */
@Service
public class AutenticacionService {

    private static final String CREDENCIALES_INCORRECTAS = "Usuario o contraseña incorrectos.";

    private final UserDetailsService usuarios;
    private final PasswordEncoder passwordEncoder;
    private final EmpleadoRepository empleadoRepository;

    public AutenticacionService(UserDetailsService usuarios, PasswordEncoder passwordEncoder,
                                EmpleadoRepository empleadoRepository) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.empleadoRepository = empleadoRepository;
    }

    /**
     * Si todo está bien, deja la Sesion iniciada.
     *
     * @throws IllegalArgumentException si el usuario o la clave son incorrectos
     * @throws IllegalStateException    si el usuario existe pero no puede usar la app (cliente o sin sector)
     */
    public void ingresar(String usuario, char[] clave) {
        UserDetails cuenta;
        try {
            cuenta = usuarios.loadUserByUsername(usuario);
        } catch (UsernameNotFoundException e) {
            throw new IllegalArgumentException(CREDENCIALES_INCORRECTAS);
        }
        if (!passwordEncoder.matches(CharBuffer.wrap(clave), cuenta.getPassword())) {
            throw new IllegalArgumentException(CREDENCIALES_INCORRECTAS);
        }

        Rol rol = cuenta.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(Rol::desdeAutoridad)
                .findFirst()
                .orElse(Rol.EMPLEADO);

        if (rol == Rol.CLIENTE) {
            throw new IllegalStateException(
                    "Esta aplicación es para el personal del restaurante. Los clientes usan la página web.");
        }
        if (!rol.puedeUsarEscritorio()) {
            throw new IllegalStateException(
                    "Tu usuario no tiene un sector con permisos. Consultá con Recursos Humanos.");
        }

        Empleado empleado = empleadoRepository.findByUsername(cuenta.getUsername());
        String nombre = ((empleado.getNombre() == null ? "" : empleado.getNombre()) + " "
                + (empleado.getApellido() == null ? "" : empleado.getApellido())).trim();
        int nroRestaurante = empleado.getSector() != null && empleado.getSector().getRestaurante() != null
                ? empleado.getSector().getRestaurante().getNroRestaurante()
                : 0;

        Sesion.iniciar(empleado.getUsername(), nombre, rol, empleado.getLegajo(), nroRestaurante);
    }
}
