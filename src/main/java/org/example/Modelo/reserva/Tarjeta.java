package org.example.Modelo.reserva;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.YearMonth;
@Entity
@Getter
@Setter @NoArgsConstructor
public class Tarjeta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int nroTarjeta;
    private String titular;
    protected int cvv;
    protected YearMonth vencimiento;
    public boolean esValida(){
        return vencimiento.isAfter(YearMonth.now());
    }
}
