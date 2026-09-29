package org.example.repository.persona;

import org.example.Modelo.persona.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    // el cliente sin cuenta se identifica por su DNI
    Optional<Cliente> findFirstByDni(int dni);
    Optional<Cliente> findByUsername(String username);
}
