package org.example.repository.inventario;

import org.example.Modelo.inventario.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
}