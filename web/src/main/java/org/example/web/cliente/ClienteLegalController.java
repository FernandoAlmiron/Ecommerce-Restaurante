package org.example.web.cliente;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Política de privacidad y términos y condiciones. Son públicas (se leen antes de registrarse):
 *  ver SeguridadWebConfig. Los links están en el footer de todas las páginas.
 */
@Controller
public class ClienteLegalController {

    @GetMapping("/privacidad")
    public String privacidad() {
        return "cliente/legal/privacidad";
    }

    @GetMapping("/terminos")
    public String terminos() {
        return "cliente/legal/terminos";
    }
}
