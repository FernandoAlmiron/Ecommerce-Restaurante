package org.example.web.cliente;

import org.example.service.persona.ClienteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientePerfilController {

    private final ClienteService clienteService;

    public ClientePerfilController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // los datos del cliente logueado (solo lectura)
    @GetMapping("/perfil")
    public String perfil(Authentication auth, Model model) {
        model.addAttribute("c", clienteService.buscarPorUsername(auth.getName()));
        return "cliente/cuenta/perfil";
    }
}
