package org.example.web.cliente;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/** Es pública (no hace falta iniciar sesión): ver SeguridadWebConfig.
 */
@Controller
public class ClienteNosotrosController {

    // iniciales, nombre, mail de cada integrante
    private static final List<String[]> EQUIPO = List.of(
            new String[]{"CA", "Carlos Alvarez Puerta", "calvarezpuerta@uade.edu.ar"},
            new String[]{"CL", "Cruz Lobo",             "clobo@uade.edu.ar"},
            new String[]{"FA", "Fernando Almirón",      "falmiron@uade.edu.ar"},
            new String[]{"JR", "Joaquín Ramallo",       "jramallo@uade.edu.ar"}
    );

    @GetMapping("/nosotros")
    public String nosotros(Model model) {
        model.addAttribute("equipo", EQUIPO);
        return "cliente/inicio/nosotros";
    }
}
