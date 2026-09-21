package org.example.Modelo.reserva;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.config.YearMonthConverter;

import java.time.YearMonth;

@Entity
@Getter
@Setter @NoArgsConstructor
public class Tarjeta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int nroTarjeta;
    private String titular;
    @Convert(converter = YearMonthConverter.class)
    protected YearMonth vencimiento;
    public boolean esValida(){
        // una tarjeta vence el ultimo dia del mes que dice: sigue valida durante ese mes
        return vencimiento != null && !vencimiento.isBefore(YearMonth.now());
    }
}
