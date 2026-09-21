package org.example.Modelo.menu;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Modelo.facturacion.Ticket;
import java.math.BigDecimal;
@Entity
@Getter @Setter @NoArgsConstructor
public class Pedido {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int idPedido;
    @ManyToOne
    @JoinColumn(name = "idMenu")
    private Menu menu;
    @ManyToOne
    @JoinColumn(name = "nroTicket")
    @JsonBackReference
    private Ticket ticket;
    private int cantidad;
    @Column(precision = 10, scale = 2)
    private BigDecimal precioUnitario;
    private String observaciones;

    public BigDecimal calcularSubtotal(){
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
