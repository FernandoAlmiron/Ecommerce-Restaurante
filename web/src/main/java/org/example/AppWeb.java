package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Arranca el front web (HTMX + Thymeleaf).
 *
 * Igual que AppEscritorio: usa las entidades, repositorios y services del backend,
 * que llegan como dependencia en el pom. Va en el paquete org.example (y no en org.example.web)
 * para que Spring encuentre también las clases del backend.
 *
 * Del backend se cargan todos los componentes MENOS:
 *  - el Main del backend (si no, haría su propio escaneo y traería todo de vuelta)
 *  - los controllers REST (/api/...): esos los sirve el backend en el puerto 8080
 * La seguridad (SecurityConfig) y el ManejadorErrores SÍ se usan acá.
 *
 * Corre en el puerto 8081 (ver web.properties), así puede estar prendido junto al backend.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "org.example", excludeFilters = {
        // los dos filtros que Spring Boot pone siempre
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class),
        // lo que es solo del backend
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
                "org\\.example\\.Main",
                "org\\.example\\.controller\\..*"})
})
public class AppWeb {

    public static void main(String[] args) {
        // primero el application.properties del backend (conexión a MySQL) y después web.properties
        System.setProperty("spring.config.name", "application,web");
        SpringApplication.run(AppWeb.class, args);
    }
}
