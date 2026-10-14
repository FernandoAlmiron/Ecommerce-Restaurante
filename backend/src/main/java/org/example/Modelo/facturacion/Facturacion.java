package org.example.Modelo.facturacion;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.example.Modelo.enums.MetodoPago;
import org.example.Modelo.enums.EstadoPago;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;
    @Column(precision = 12, scale = 2)
    private BigDecimal montoTotal = BigDecimal.ZERO;
    private LocalDateTime fecha;
    @Column(precision = 12, scale = 2)
    private BigDecimal propina = BigDecimal.ZERO;
    private Integer porcentajePropina;

    @Enumerated(EnumType.STRING)
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;
    private LocalDateTime fechaPago;

    // Lo que el cliente tiene que pagar: consumo + propina
    public BigDecimal getMontoAPagar() {
        return this.montoTotal.add(this.propina);
    }

    public void aplicarPropina(int porcentaje) {
        exigirPendiente();
        if (porcentaje != 5 && porcentaje != 10 && porcentaje != 15) {
            throw new IllegalArgumentException("La propina solo puede ser 5%, 10% o 15%");
        }
        this.porcentajePropina = porcentaje;
        this.propina = calcularPropina(porcentaje);
    }

    public void confirmarPago() {
        exigirPendiente();
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

    // Recalcula el consumo desde los pedidos del ticket y, si ya habia propina, la actualiza
    public BigDecimal calcularTotal(){
        exigirPendiente();
        montoTotal = ticket.calcularTotal();
        if (porcentajePropina != null) {
            propina = calcularPropina(porcentajePropina);
        }
        return montoTotal;
    }

    // porcentaje del consumo, redondeado a centavos
    private BigDecimal calcularPropina(int porcentaje) {
        return montoTotal.multiply(BigDecimal.valueOf(porcentaje))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    // Una facturacion ya pagada no se puede modificar ni volver a cobrar
    private void exigirPendiente() {
        if (estadoPago == EstadoPago.PAGADO) {
            throw new IllegalStateException("La facturacion ya esta pagada");
        }
    }

}