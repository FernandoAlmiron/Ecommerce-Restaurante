package org.example.service;

import org.example.Modelo.Restaurante;
import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.reserva.Reserva;
import org.example.Modelo.reserva.Zona;
import org.example.repository.RestauranteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class RestauranteService {
    private final RestauranteRepository restauranteRepository;

    public RestauranteService(RestauranteRepository restauranteRepository) {
        this.restauranteRepository = restauranteRepository;
    }
    public Restaurante agregarReserva(int nroRestaurante, Reserva reserva){
        Restaurante restaurante = restauranteRepository.findById(nroRestaurante).orElseThrow(()-> new java.util.NoSuchElementException("Restaurante no encontrado"));
        restaurante.agregarReserva(reserva);
        return restauranteRepository.save(restaurante);
    }
    public List<Ticket> listarTicketsPorFecha(int nroRestaurante, LocalDate fecha) {
        Restaurante restaurante = restauranteRepository.findById(nroRestaurante)
                .orElseThrow(() -> new java.util.NoSuchElementException("Restaurante no encontrado"));
        return restaurante.listarTicketsPorFecha(fecha);
    }

    public boolean estaAbierto(int nroRestaurante, LocalTime hora) {
        Restaurante restaurante = restauranteRepository.findById(nroRestaurante)
                .orElseThrow(() -> new java.util.NoSuchElementException("Restaurante no encontrado"));
        return restaurante.estaAbierto(hora);
    }

    public boolean estaDisponible(int nroRestaurante, Zona zona, LocalDate fecha, LocalTime hora) {
        Restaurante restaurante = restauranteRepository.findById(nroRestaurante)
                .orElseThrow(() -> new java.util.NoSuchElementException("Restaurante no encontrado"));
        return restaurante.estaDisponible(zona, fecha, hora);
    }
    public Restaurante guardar(Restaurante restaurante) {
        return restauranteRepository.save(restaurante);
    }

    public Restaurante buscarPorId(int nroRestaurante) {
        return restauranteRepository.findById(nroRestaurante)
                .orElseThrow(() -> new java.util.NoSuchElementException("Restaurante no encontrado"));
    }

    public List<Restaurante> listarTodos() {
        return restauranteRepository.findAll();
    }

    public Restaurante actualizar(int nroRestaurante, Restaurante datos) {
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException("El restaurante necesita un nombre");
        }
        if (datos.getHorarioApertura() == null || datos.getHorarioCierre() == null) {
            throw new IllegalArgumentException("El restaurante necesita horario de apertura y de cierre");
        }
        Restaurante actual = buscarPorId(nroRestaurante);   // 404 si no existe
        actual.setNombre(datos.getNombre());
        actual.setDireccion(datos.getDireccion());
        actual.setTelefono(datos.getTelefono());
        actual.setEmail(datos.getEmail());
        actual.setHorarioApertura(datos.getHorarioApertura());
        actual.setHorarioCierre(datos.getHorarioCierre());
        return restauranteRepository.save(actual);
    }
}
