package org.example.Modelo.persona;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter @Setter @NoArgsConstructor
public abstract class Persona {
    protected String nombre;
    protected String apellido;
    protected String celular;
    protected int dni;

    /**
     * El celular de un cliente o empleado: opcional, y si está, 10 números (código de área + número,
     * ej. 1155551234). Se aceptan espacios y guiones al escribirlo ("11 5555-1234") y se guarda sin ellos.
     * @return el celular solo con números, o null si vino vacío
     */
    public static String normalizarCelular(String celular) {
        String numeros = celular == null ? "" : celular.replaceAll("[\\s-]", "");
        if (numeros.isEmpty()) {
            return null;
        }
        if (!numeros.matches("\\d{10}")) {
            throw new IllegalArgumentException("El celular tiene que tener 10 números (con el código de área, sin el 0 ni el 15)");
        }
        return numeros;
    }

    public Persona(String nombre, String apellido, String celular, int dni){
        this.nombre= nombre;
        this.apellido=apellido;
        this.celular=celular;
        this.dni=dni;
    }
}