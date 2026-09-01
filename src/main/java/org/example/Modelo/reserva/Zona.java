package org.example.Modelo.reserva;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Modelo.Restaurante;

@Entity
@Getter @Setter @NoArgsConstructor
public class Zona {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int idZona;
    private String nombre;
    private int capacidadMaxima;
    private boolean activa;
    @ManyToOne
    @JoinColumn(name = "nroRestaurante")
    @JsonBackReference
    private Restaurante restaurante;

    public boolean tieneCapacidad(int cantComensales){
        return activa && cantComensales <= capacidadMaxima;
    }
}
