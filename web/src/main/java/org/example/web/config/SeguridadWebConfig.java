package org.example.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Configuration
public class SeguridadWebConfig {

    // encripta las contraseñas (lo usa ClienteService al registrar)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filtroWeb(HttpSecurity http) throws Exception {
        http
                // HTMX manda PUT/DELETE sin token CSRF: lo desactivamos (igual que el backend)
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // páginas públicas: cualquiera, sin login
                        .requestMatchers("/", "/login", "/registro", "/nosotros", "/error").permitAll()
                        // archivos estáticos (static/css, static/js, static/img)
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                        // el repartidor: solo su pantalla de entregas
                        .requestMatchers("/reparto/**").hasRole("REPARTIDOR")
                        // todo lo demás: cliente logueado
                        .anyRequest().hasRole("CLIENTE")
                )

                .formLogin(form -> form
                        .loginPage("/login")              // nuestra página, no la de Spring
                        .usernameParameter("email")       // el input se llama "email"
                        .failureUrl("/login?error")
                        // según quién entra, a dónde lo mando. La web es para los clientes (y la pantalla
                        // del repartidor): el resto de los empleados usa la app de escritorio, así que
                        // se le cierra la sesión y ve el mismo error que con una clave incorrecta.
                        .successHandler((request, response, auth) -> {
                            if (tieneRol(auth, "ROLE_REPARTIDOR")) {
                                response.sendRedirect("/reparto");
                            } else if (tieneRol(auth, "ROLE_CLIENTE")) {
                                response.sendRedirect("/inicio");
                            } else {
                                new SecurityContextLogoutHandler().logout(request, response, auth);
                                response.sendRedirect("/login?error");
                            }
                        })
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }

    private static boolean tieneRol(Authentication auth, String rol) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(rol));
    }
}
