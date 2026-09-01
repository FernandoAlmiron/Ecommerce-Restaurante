package org.example.Modelo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.example.Modelo.enums.EstadoReserva;
import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.inventario.Stock;
import org.example.Modelo.menu.Menu;
import org.example.Modelo.persona.Cliente;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Modelo.persona.Sector;
import org.example.Modelo.reserva.Reserva;
import org.example.Modelo.reserva.Zona;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;
import java.time.LocalDate;
@Entity
@Getter @Setter @NoArgsConstructor
public class Restaurante {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int nroRestaurante;
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private LocalTime horarioApertura;
    private LocalTime horarioCierre;
    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Reserva> reservas;

    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Cliente> clientes;
    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Ticket> tickets;
    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Zona> zonas;
    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Stock> stocks;
    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Sector> sectores;
    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Menu> menus;
    public void agregarReserva(Reserva reserva){
        reservas.add(reserva);
    }
    public void agregarZona(Zona zona){
        zonas.add(zona);
    }
    public void agregarMenu(Menu menu){
        menus.add(menu);
    }
    public boolean estaAbierto(LocalTime hora){
        return !hora.isBefore(horarioApertura)&& !hora.isAfter(horarioCierre);
    }
    public List<Ticket> listarTicketsPorFecha(LocalDate fecha){
        List<Ticket> resultado=new ArrayList<>();
        for (Ticket ticket : tickets){
            if (ticket.getReserva() != null && ticket.getReserva().getFechaReserva().equals(fecha)){
                resultado.add(ticket);
            }
        }
        return resultado;
    }
    public boolean estaDisponible(Zona zona, LocalDate fecha, LocalTime hora) {
        for (Reserva reserva : reservas) {
            if (reserva.getZona() == zona && reserva.getFechaReserva().equals(fecha) && reserva.getHoraReserva().equals(hora) && reserva.getEstado() != EstadoReserva.CANCELADA) {
                return false;
            }
        }
        return true;
    }

}
