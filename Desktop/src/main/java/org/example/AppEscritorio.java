package org.example;

import org.example.escritorio.Contexto;
import org.example.escritorio.vista.LoginFrame;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Arranca la app de escritorio.
 *
 * Levanta Spring SIN servidor web para usar las mismas entidades, repositorios y services
 * del backend. Va en el paquete org.example (y no en org.example.escritorio) para que Spring
 * encuentre las entidades y repositorios del backend.
 *
 * Del backend se cargan todos los componentes MENOS los que son solo del servidor web:
 * el Main del backend, los controllers REST, la web del cliente, SecurityConfig y ManejadorErrores.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "org.example", excludeFilters = {
        // los dos filtros que Spring Boot pone siempre
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class),
        // lo que es solo del servidor web
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
                "org\\.example\\.Main",
                "org\\.example\\.controller\\..*",
                "org\\.example\\.web\\..*",
                "org\\.example\\.config\\.SecurityConfig",
                "org\\.example\\.config\\.ManejadorErrores"})
})
public class AppEscritorio {

    public static void main(String[] args) {
        aplicarAspecto();
        JWindow aviso = mostrarAviso();

        // primero el application.properties del backend (conexión a MySQL) y después escritorio.properties
        System.setProperty("spring.config.name", "application,escritorio");

        ConfigurableApplicationContext contexto;
        try {
            contexto = new SpringApplicationBuilder(AppEscritorio.class)
                    .web(WebApplicationType.NONE) // sin Tomcat: esto no es un servidor
                    .headless(false)              // Spring por defecto desactiva las ventanas
                    .run(args);
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                aviso.dispose();
                Dialogos.error(null, "No se pudo iniciar la aplicación.\n"
                        + "Verificá que MySQL esté encendido y que los datos de conexión sean correctos.");
                System.exit(1);
            });
            return;
        }

        Contexto.iniciar(contexto);
        SwingUtilities.invokeLater(() -> {
            aviso.dispose();
            new LoginFrame().setVisible(true);
        });
    }

    /** Ventanita mientras Spring arranca y se conecta a la base (tarda unos segundos). */
    private static JWindow mostrarAviso() {
        JWindow[] ventana = new JWindow[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                JLabel texto = new JLabel("Conectando con la base de datos...", SwingConstants.CENTER);
                texto.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));
                JWindow w = new JWindow();
                w.add(texto);
                w.pack();
                w.setLocationRelativeTo(null);
                w.setVisible(true);
                ventana[0] = w;
            });
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo crear la ventana de inicio", e);
        }
        return ventana[0];
    }

    /**
     * Usa FlatLaf con el tema (claro u oscuro) que se eligió la última vez en esta PC;
     * si FlatLaf no está en el classpath, el aspecto del sistema operativo.
     */
    private static void aplicarAspecto() {
        Estilo.usarTema(Estilo.temaGuardadoEsOscuro());
    }
}