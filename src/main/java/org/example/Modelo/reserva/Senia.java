package org.example.Modelo.reserva;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Senia {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int idSenia;
    private double monto;
    private boolean asistio;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "nroTarjeta")
    private Tarjeta tarjeta;
    @OneToOne
    @JoinColumn(name = "nroReserva")
    @JsonBackReference
    private Reserva reserva;

    public void cobraPorInasistencia(){
        if(!asistio){
            if(tarjeta.esValida()){
                System.out.println("Cobrando $"+ monto+" a la tarjeta de "+ tarjeta.getTitular() +" por inasistencia.");
            }else{
                System.out.println("No se pudo cobrar la seña: la tarjeta no es valida.");
            }
        }

    }
}
