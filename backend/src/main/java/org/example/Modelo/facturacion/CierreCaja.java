package org.example.Modelo.facturacion;

import org.example.Modelo.persona.Empleado;
import org.example.Modelo.Restaurante;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor
public class CierreCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idCierre;

    private LocalDate fecha;
    @Column(precision = 12, scale = 2)
    private BigDecimal montoEsperado = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2)
    private BigDecimal montoContado = BigDecimal.ZERO;
    @Column(precision = 12, scale = 2)
    private BigDecimal diferencia = BigDecimal.ZERO;
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "legajoCajera")
    private Empleado cajera;

    @ManyToOne
    @JoinColumn(name = "nroRestaurante")
    private Restaurante restaurante;

    public void calcularDiferencia() {
        this.diferencia = this.montoContado.subtract(this.montoEsperado);
    }

    public boolean faltoPlata() { return this.diferencia.signum() < 0; }
    public boolean sobroPlata() { return this.diferencia.signum() > 0; }
}