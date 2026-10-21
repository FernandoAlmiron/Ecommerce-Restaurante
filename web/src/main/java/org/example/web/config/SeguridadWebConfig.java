package org.example.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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

                        .requestMatchers("/", "/login", "/registro", "/error").permitAll()
                        .requestMatchers("/errores.js", "/style.css", "/css/**", "/img/**").permitAll()

                        // panel del restaurante

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // todo lo demás: cliente logueado

                        .anyRequest().hasRole("CLIENTE")
                )

                .formLogin(form -> form
                        .loginPage("/login")              // nuestra página, no la de Spring
                        .usernameParameter("email")       // el input se llama "email"
                        .failureUrl("/login?error")

                        // según quién entra, a dónde lo mando
                        .successHandler((request, response, auth) -> {
                            boolean esAdmin = auth.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                            response.sendRedirect(esAdmin ? "/admin" : "/elegir");
                        })
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }
}