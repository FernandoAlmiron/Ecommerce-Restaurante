package org.example.Modelo.persona;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    // cuenta opcional: con usuario y clave, el cliente pide sin volver a dar sus datos
    @Column(unique = true)
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @ManyToOne
    @JoinColumn(name = "nroRestaurante")
    private Restaurante restaurante;
    public Cliente(String nombre,String apellido,String celular,int dni,String email){
        super(nombre,apellido,celular,dni);
        this.email=email;
    }
}
