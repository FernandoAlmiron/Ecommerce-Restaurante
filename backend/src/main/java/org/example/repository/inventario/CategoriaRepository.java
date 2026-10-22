package org.example.repository.inventario;

import org.example.Modelo.inventario.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    /** Si ya hay otra categoría con ese nombre (idPropio = la que se guarda, 0 si es nueva). */
    boolean existsByNombreIgnoreCaseAndNroCategoriaNot(String nombre, int idPropio);
}