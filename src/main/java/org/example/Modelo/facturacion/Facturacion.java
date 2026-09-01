package org.example.Modelo.facturacion;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.example.Modelo.enums.MetodoPago;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Getter @Setter @NoArgsConstructor
public class Facturacion {
    @ManyToOne
    @JoinColumn(name = "nroTicket")
    @JsonBackReference
    private Ticket ticket;
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int nroFacturacion;
    private MetodoPago metodoPago;
    private double montoTotal;
    private LocalDateTime fecha;

    public void imprimirTicket(){
        System.out.println("Ticket N°: " + ticket.getNroTicket());
        System.out.println("Fecha: " + fecha);
        System.out.println("Total: $" + montoTotal);
        System.out.println("Metodo de pago: " + metodoPago);
        if (metodoPago == MetodoPago.TARJETA) {
            System.out.println("Pago realizado con tarjeta");
        } else if (metodoPago == MetodoPago.EFECTIVO) {
            System.out.println("Pago realizado en efectivo");
        } else if (metodoPago == MetodoPago.QR) {
            System.out.println("Pago realizado por QR");
        }

    }
    public double calcularTotal(){
        montoTotal= ticket.calcularTotal();
        return montoTotal;
    }
}
