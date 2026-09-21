package org.example.Modelo.facturacion;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.example.Modelo.Restaurante;
import org.example.Modelo.menu.Pedido;
import org.example.Modelo.reserva.Reserva;
import org.example.Modelo.enums.OrigenTicket;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor
public class Ticket {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int nroTicket;
    @ManyToOne
    @JoinColumn(name = "nroReserva")
    private Reserva reserva;
    @ManyToOne
    @JoinColumn(name = "nroRestaurante")
    private Restaurante restaurante;

    @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Facturacion facturacion;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Pedido> pedidos;
    @Enumerated(EnumType.STRING)
    private OrigenTicket origen = OrigenTicket.SALON;

    public void agregarPedido(Pedido pedido){
        pedidos.add(pedido);
    }
    public BigDecimal calcularTotal(){
        BigDecimal total= BigDecimal.ZERO;
        for(Pedido pedido : pedidos){
            total = total.add(pedido.calcularSubtotal());
        }
        return total;
    }
}
