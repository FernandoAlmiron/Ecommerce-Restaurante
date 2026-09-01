package org.example.repository.persona;

import org.example.Modelo.persona.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    Empleado findByUsername(String username);
}