package org.example.dto;

import org.example.Modelo.enums.EstadoReserva;
import org.example.Modelo.reserva.Reserva;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaResponseDTO(
        int nroReserva,
        LocalDate fecha,
        LocalTime hora,
        int comensales,
        String cliente,
        String zona,
        EstadoReserva estado,
        BigDecimal senia,
        Boolean asistio
) {
    public static ReservaResponseDTO desde(Reserva r) {
        return new ReservaResponseDTO(
                r.getNroReserva(),
                r.getFechaReserva(),
                r.getHoraReserva(),
                r.getCantComensales(),
                r.getCliente().getNombre() + " " + r.getCliente().getApellido(),
                r.getZona().getNombre(),
                r.getEstado(),
                r.getSenia() != null ? r.getSenia().getMonto() : null,
                r.getSenia() != null ? r.getSenia().isAsistio() : null
        );
    }
}
