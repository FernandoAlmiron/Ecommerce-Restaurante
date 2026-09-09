package org.example.Modelo.reserva;

import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.persona.Cliente;
import org.example.Modelo.persona.Empleado;
import org.example.Modelo.enums.EstadoEnvio;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Getter @Setter @NoArgsConstructor
public class Envio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idEnvio;

    private String direccionEntrega;
    private String nombreReceptor;

    @Enumerated(EnumType.STRING)
    private EstadoEnvio estado;

    @ManyToOne
    @JoinColumn(name = "idCliente")
    private Cliente cliente;

    @OneToOne
    @JoinColumn(name = "nroTicket")
    @JsonManagedReference
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "legajoRepartidor")
    private Empleado repartidor;

    public void marcarEnCamino() { this.estado = EstadoEnvio.EN_CAMINO; }
    public void marcarEntregado() { this.estado = EstadoEnvio.ENTREGADO; }
    public void cancelar() { this.estado = EstadoEnvio.CANCELADO; }
}
