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

    public Persona(String nombre, String apellido, String celular, int dni){
        this.nombre= nombre;
        this.apellido=apellido;
        this.celular=celular;
        this.dni=dni;
    }
}