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
    private EstadoReserva estado;
    @ManyToOne
    @JoinColumn(name = "idCliente")
    private Cliente cliente;
    @ManyToOne
    @JoinColumn(name = "idZona")
    private Zona zona;
    @ManyToOne
    @JoinColumn(name = "nroRestaurante")
    @JsonBackReference
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
        senia.setAsistio(asistio);}

}
