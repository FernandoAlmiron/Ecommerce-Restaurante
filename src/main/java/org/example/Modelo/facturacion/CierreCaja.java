package org.example.Modelo.facturacion;

import org.example.Modelo.persona.Empleado;
import org.example.Modelo.Restaurante;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor
public class CierreCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idCierre;

    private LocalDate fecha;
    private double montoEsperado;
    private double montoContado;
    private double diferencia;
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "legajoCajera")
    private Empleado cajera;

    @ManyToOne
    @JoinColumn(name = "nroRestaurante")
    @JsonBackReference
    private Restaurante restaurante;

    public void calcularDiferencia() {
        this.diferencia = this.montoContado - this.montoEsperado;
    }

    public boolean faltoPlata() { return this.diferencia < 0; }
    public boolean sobroPlata() { return this.diferencia > 0; }
}