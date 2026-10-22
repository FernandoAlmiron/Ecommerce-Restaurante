package org.example.repository.reserva;

import org.example.Modelo.reserva.Zona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZonaRepository extends JpaRepository<Zona, Integer> {

    List<Zona> findByRestaurante_NroRestauranteAndActivaTrueOrderByNombre(int nroRestaurante);

    /** Si el restaurante ya tiene otra zona con ese nombre (idPropio = la que se guarda, 0 si es nueva). */
    boolean existsByNombreIgnoreCaseAndRestaurante_NroRestauranteAndIdZonaNot(String nombre, int nroRestaurante,
                                                                             int idPropio);
}
