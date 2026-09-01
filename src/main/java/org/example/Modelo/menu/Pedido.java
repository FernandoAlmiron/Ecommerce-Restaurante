package org.example.Modelo.menu;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Modelo.facturacion.Ticket;

@Entity
@Getter @Setter @NoArgsConstructor
public class Pedido {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int idPedido;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idMenu")
    private Menu menu;
    @ManyToOne
    @JoinColumn(name = "nroTicket")
    @JsonBackReference
    private Ticket ticket;
    private int cantidad;
    private double precioUnitario;
    private String observaciones;

    public double calcularSubtotal(){
        return cantidad*precioUnitario;
    }
}
