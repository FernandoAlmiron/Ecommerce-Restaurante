package org.example.Modelo.persona;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Modelo.Restaurante;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "Clientes")
public class Cliente extends Persona{
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int idCliente;
    private String email;
    @ManyToOne
    @JoinColumn(name = "nroRestaurante")
    @JsonBackReference
    private Restaurante restaurante;
    public Cliente(String nombre,String apellido,int celular,int dni,String email){
        super(nombre,apellido,celular,dni);
        this.email=email;
    }
}
