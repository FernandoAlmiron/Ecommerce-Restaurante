package org.example.web.cliente;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.example.Modelo.persona.Cliente;
import org.example.service.persona.ClienteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ClientePerfilController {

    private final ClienteService clienteService;

    public ClientePerfilController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // los datos del cliente logueado, para verlos y cambiarlos
    @GetMapping("/perfil")
    public String perfil(Authentication auth, Model model) {
        model.addAttribute("c", clienteService.buscarPorUsername(auth.getName()));
        return "cliente/cuenta/perfil";
    }

    // guarda nombre, apellido, email y celular (el DNI no se cambia)
    @PostMapping("/perfil")
    public String guardar(@RequestParam String nombre, @RequestParam String apellido,
                          @RequestParam String email, @RequestParam(required = false) String celular,
                          Authentication auth, HttpServletRequest request, Model model) throws ServletException {
        try {
            Cliente guardado = clienteService.actualizarPerfil(auth.getName(), nombre, apellido, email, celular);
            if (!guardado.getUsername().equals(auth.getName())) {
                // se entra con el email: si cambió, se vuelve a iniciar sesión con el nuevo
                request.logout();
                return "redirect:/login?emailCambiado";
            }
            return "redirect:/perfil?guardado";
        } catch (IllegalArgumentException | IllegalStateException e) {
            // vuelve al perfil con el mensaje y lo que había escrito (lo muestra el template con param)
            model.addAttribute("error", e.getMessage());
            model.addAttribute("c", clienteService.buscarPorUsername(auth.getName()));
            return "cliente/cuenta/perfil";
        }
    }

    // cambio de contraseña: pide la actual
    @PostMapping("/perfil/clave")
    public String cambiarClave(@RequestParam String claveActual, @RequestParam String claveNueva,
                               @RequestParam String claveNueva2, Authentication auth, Model model) {
        try {
            if (!claveNueva.equals(claveNueva2)) {
                throw new IllegalArgumentException("Las contraseñas nuevas no coinciden");
            }
            clienteService.cambiarClave(auth.getName(), claveActual, claveNueva);
            return "redirect:/perfil?claveCambiada";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorClave", e.getMessage());
            model.addAttribute("c", clienteService.buscarPorUsername(auth.getName()));
            return "cliente/cuenta/perfil";
        }
    }
}
