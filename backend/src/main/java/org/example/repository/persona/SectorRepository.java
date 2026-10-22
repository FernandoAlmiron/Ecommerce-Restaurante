package org.example.repository.persona;

import org.example.Modelo.persona.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorRepository extends JpaRepository<Sector, Integer> {

    /** Si el restaurante ya tiene otro sector con ese nombre (idPropio = el que se guarda, 0 si es nuevo). */
    boolean existsByNombreIgnoreCaseAndRestaurante_NroRestauranteAndIdSectorNot(String nombre, int nroRestaurante,
                                                                                 int idPropio);
}
