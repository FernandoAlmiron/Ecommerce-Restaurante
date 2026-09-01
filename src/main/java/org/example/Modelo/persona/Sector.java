package org.example.Modelo.persona;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Modelo.Restaurante;

@Entity
@Getter @Setter @NoArgsConstructor
public class Sector {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idSector;
    private String nombre;
    private boolean activa;
    @ManyToOne
    @JoinColumn(name = "nroRestaurante")
    @JsonBackReference
    private Restaurante restaurante;
    public void activar(){
        activa=true;
    }
    public void desactivar(){
        activa=false;
    }
}
