package org.example.Modelo.facturacion;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.example.Modelo.Restaurante;
import org.example.Modelo.menu.Pedido;
import org.example.Modelo.reserva.Reserva;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor
public class Ticket {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int nroTicket;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "nroReserva")
    private Reserva reserva;
    @ManyToOne
    @JoinColumn(name = "nroRestaurante")
    @JsonBackReference
    private Restaurante restaurante;

    @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Facturacion facturacion;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Pedido> pedidos;

    public void agregarPedido(Pedido pedido){
        pedidos.add(pedido);
    }
    public double calcularTotal(){
        double total=0;
        for(Pedido pedido : pedidos){
            total += pedido.calcularSubtotal();
        }
        return total;
    }
}
