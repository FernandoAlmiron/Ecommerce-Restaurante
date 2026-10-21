package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "org.example", excludeFilters = {
        // los dos filtros que Spring Boot pone siempre
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class),
        // lo que es solo del backend
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
                "org\\.example\\.Main",
                "org\\.example\\.controller\\..*",
                "org\\.example\\.config\\.SecurityConfig"})

public class AppWeb {

    public static void main(String[] args) {
        System.setProperty("spring.config.name", "application,web");
        SpringApplication.run(AppWeb.class, args);
    }
}
