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

    /**
     * Un teléfono de 10 números listo para mostrar: 11 4300-1100 (código de área 11) o 351 430-1100
     * (código de área de 3). Se guarda sin espacios ni guiones; esto es solo para verlo.
     * Si no tiene 10 números se devuelve como está.
     */
    public static String formatearTelefono(String telefono) {
        if (telefono == null) {
            return null;
        }
        String numeros = telefono.replaceAll("\\D", "");
        if (numeros.length() != 10) {
            return telefono;
        }
        int area = numeros.startsWith("11") ? 2 : 3;
        return numeros.substring(0, area) + " " + numeros.substring(area, 6) + "-" + numeros.substring(6);
    }

    public Persona(String nombre, String apellido, String celular, int dni){
        this.nombre= nombre;
        this.apellido=apellido;
        this.celular=celular;
        this.dni=dni;
    }
}