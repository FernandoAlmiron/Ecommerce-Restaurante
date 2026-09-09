package org.example.Modelo.facturacion;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.example.Modelo.enums.MetodoPago;
import org.example.Modelo.enums.EstadoPago;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Facturacion {
    @OneToOne
    @JoinColumn(name = "nroTicket")
    @JsonBackReference
    private Ticket ticket;
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int nroFacturacion;
    private MetodoPago metodoPago;
    private double montoTotal;
    private LocalDateTime fecha;
    private double propina = 0;
    private Integer porcentajePropina;

    @Enumerated(EnumType.STRING)
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;
    private LocalDateTime fechaPago;

    public double getMontoAPagar() {
        return this.montoTotal;
    }

    public void aplicarPropina(int porcentaje) {
        if (porcentaje != 5 && porcentaje != 10 && porcentaje != 15) {
            throw new IllegalArgumentException("La propina solo puede ser 5%, 10% o 15%");
        }
        this.porcentajePropina = porcentaje;
        this.propina = this.montoTotal * porcentaje / 100.0;
    }

    public void confirmarPago() {
        this.estadoPago = EstadoPago.PAGADO;
        this.fechaPago = LocalDateTime.now();
    }

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
