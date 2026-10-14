package org.example.Modelo.reserva;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.example.Modelo.Restaurante;
import org.example.Modelo.persona.Cliente;
import org.example.Modelo.enums.EstadoReserva;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
@Entity
@Getter @Setter @NoArgsConstructor
public class Reserva {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int nroReserva;

    private int cantComensales;
    private LocalDate fechaReserva;
    private LocalTime horaReserva;
    @Enumerated(EnumType.STRING)
    private EstadoReserva estado;
    @ManyToOne
    @JoinColumn(name = "idCliente")
    private Cliente cliente;
    @ManyToOne
    @JoinColumn(name = "idZona")
    private Zona zona;
    @ManyToOne
    @JoinColumn(name = "nroRestaurante")
    private Restaurante restaurante;
    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Senia senia;

    public void confirmar(){
        estado=EstadoReserva.CONFIRMADA;
    }
    public void cancelar(){
        estado=EstadoReserva.CANCELADA;
    }
    public void marcarAsistencia(boolean asistio) {
        // los walk-in no tienen sena: sin este control daba NullPointerException (500)
        if (senia == null) {
            throw new IllegalStateException("La reserva no tiene sena: no se registra asistencia");
        }
        senia.setAsistio(asistio);
    }

}
