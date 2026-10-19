package org.example.escritorio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * En el backend, el PasswordEncoder se declara dentro de SecurityConfig. La app de escritorio no carga
 * SecurityConfig (es la seguridad del servidor web), así que el encoder se declara acá.
 * Es el mismo algoritmo (BCrypt), así que las claves guardadas por la API y por Swing son compatibles.
 * Lo usan ClienteService y EmpleadoService del backend, y el login de Swing.
 */
@Configuration
public class SeguridadEscritorioConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
