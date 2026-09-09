package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // cliente sin cuenta
                        .requestMatchers(HttpMethod.POST, "/api/reservas").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/clientes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menus/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurantes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/zonas/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/envios").permitAll()

                        // recepcion
                        .requestMatchers(HttpMethod.POST, "/api/reservas/walkin").hasAnyRole("ADMIN", "RECEPCION")

                        // cocina
                        .requestMatchers("/api/stocks/**").hasAnyRole("ADMIN", "COCINA")
                        .requestMatchers("/api/menu-ingredientes/**").hasAnyRole("ADMIN", "COCINA")
                        .requestMatchers(HttpMethod.PUT, "/api/menus/**").hasAnyRole("ADMIN", "COCINA")

                        // mozo
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").hasAnyRole("ADMIN", "MOZO")
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**").hasAnyRole("ADMIN", "MOZO", "CAJA")
                        .requestMatchers(HttpMethod.GET, "/api/tickets/**").hasAnyRole("ADMIN", "MOZO", "CAJA")

                        // repartidor
                        .requestMatchers(HttpMethod.GET, "/api/envios/**").hasAnyRole("ADMIN", "REPARTIDOR")
                        .requestMatchers(HttpMethod.PUT, "/api/envios/**").hasAnyRole("ADMIN", "REPARTIDOR")

                        // caja
                        .requestMatchers("/api/facturaciones/**").hasAnyRole("ADMIN", "CAJA")
                        .requestMatchers("/api/tarjetas/**").hasAnyRole("ADMIN", "CAJA")
                        .requestMatchers(HttpMethod.POST, "/api/tickets").hasAnyRole("ADMIN", "CAJA")
                        .requestMatchers("/api/cierres-caja/**").hasAnyRole("ADMIN", "CAJA")

                        //RRHH
                        .requestMatchers("/api/empleados/**").hasAnyRole("ADMIN", "RRHH")

                        // todo lo demás, solo admin
                        .anyRequest().hasRole("ADMIN")
                )
                .httpBasic(withDefaults());
        return http.build();
    }
}
