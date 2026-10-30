package org.example.web.cliente;

import org.example.Modelo.persona.Cliente;
import org.example.service.RestauranteService;
import org.example.service.persona.ClienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ClienteCuentaController {

    private final ClienteService clienteService;
    private final RestauranteService restauranteService;

    public ClienteCuentaController(ClienteService clienteService, RestauranteService restauranteService) {
        this.clienteService = clienteService;
        this.restauranteService = restauranteService;
    }

    // el formulario de login (el POST lo procesa Spring Security solo)
    @GetMapping("/login")
    public String login() {
        return "cliente/cuenta/login";
    }

    @GetMapping("/registro")
    public String registro() {
        return "cliente/cuenta/registro";
    }

    @PostMapping("/registro")
    public String registrar(@RequestParam String nombre, @RequestParam String apellido,
                            @RequestParam String email, @RequestParam int dni,
                            @RequestParam(required = false) String celular,
                            @RequestParam String password, @RequestParam String password2,
                            Model model) {

       // manejo de excepciones
        try {
            if (!password.equals(password2)) {
                throw new IllegalArgumentException("Las contraseñas no coinciden");
            }
            Cliente cliente = new Cliente();
            cliente.setNombre(nombre);
            cliente.setApellido(apellido);
            cliente.setDni(dni);
            cliente.setCelular(celular);
            cliente.setEmail(email);
            cliente.setUsername(email);          // se inicia sesión con el email
            cliente.setPassword(password);       // ClienteService la encripta
            cliente.setRestaurante(restauranteService.listarTodos().get(0));   // hay un solo local

            clienteService.guardar(cliente);     //validaciones
            return "redirect:/login?registrado";

        } catch (IllegalArgumentException | IllegalStateException e) {
            // vuelve al formulario con el mensaje de error
            model.addAttribute("error", e.getMessage());
            return "cliente/cuenta/registro";
        }
    }
}