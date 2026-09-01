package org.example.Modelo.persona;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "Empleados")
public class Empleado extends Persona{
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int legajo;
    private String username;
    private String password;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idSector")
    private Sector sector;
    public Empleado(String nombre,String apellido,int celular,int dni,int legajo){
        super(nombre,apellido,celular,dni);
        this.legajo=legajo;
    }
    public void cambiarSector(Sector nuevoSector){
        sector=nuevoSector;
    }
}
