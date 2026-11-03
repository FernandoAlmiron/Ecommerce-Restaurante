package org.example.web.config;

import org.example.Modelo.persona.Persona;
import org.springframework.stereotype.Component;

/** Para mostrar teléfonos en las plantillas: ${@telefonos.formato(...)} → 11 4300-1100. */
@Component("telefonos")
public class Telefonos {

    public String formato(String telefono) {
        return Persona.formatearTelefono(telefono);
    }
}
