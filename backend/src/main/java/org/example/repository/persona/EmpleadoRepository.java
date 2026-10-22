package org.example.repository.persona;

import org.example.Modelo.persona.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    Empleado findByUsername(String username);

    long countBySector_IdSector(int idSector);

    /** Cada fila: [idSector, cantidad de empleados]. Los sectores sin empleados no aparecen. */
    @Query("SELECT e.sector.idSector, COUNT(e) FROM Empleado e WHERE e.sector IS NOT NULL GROUP BY e.sector.idSector")
    List<Object[]> contarPorSector();
}