package org.example.Modelo.persona;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "Empleados")
public class Empleado extends Persona{
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int legajo;
    @Column(unique=true, nullable=false)
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @ManyToOne
    @JoinColumn(name = "idSector")
    private Sector sector;
    public void cambiarSector(Sector nuevoSector){
        sector=nuevoSector;
    }
}
