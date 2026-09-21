package org.example.Modelo.reserva;

import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.persona.Cliente;
import org.example.Modelo.persona.Empleado;
import org.example.Modelo.enums.EstadoEnvio;
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
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "legajoRepartidor")
    private Empleado repartidor;

    public void cambiarEstado(EstadoEnvio nuevo) {
        boolean permitido = switch (estado) {
            case PENDIENTE -> nuevo == EstadoEnvio.EN_CAMINO || nuevo == EstadoEnvio.CANCELADO;
            case EN_CAMINO -> nuevo == EstadoEnvio.ENTREGADO || nuevo == EstadoEnvio.CANCELADO;
            case ENTREGADO, CANCELADO -> false;
        };
        if (!permitido) {
            throw new IllegalStateException("No se puede pasar un envio de " + estado + " a " + nuevo);
        }
        if (nuevo == EstadoEnvio.EN_CAMINO && repartidor == null) {
            throw new IllegalStateException("Hay que asignar un repartidor antes de despachar el envio");
        }
        this.estado = nuevo;
    }

    public void marcarEnCamino() {
        cambiarEstado(EstadoEnvio.EN_CAMINO);
    }

    public void marcarEntregado() {
        cambiarEstado(EstadoEnvio.ENTREGADO);
    }

    public void cancelar() {
        cambiarEstado(EstadoEnvio.CANCELADO);
    }
}