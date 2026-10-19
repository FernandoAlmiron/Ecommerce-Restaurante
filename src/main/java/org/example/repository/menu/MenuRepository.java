package org.example.repository.menu;

import org.example.Modelo.menu.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Integer> {

    // trae solo los platos que no fueron eliminados
    List<Menu> findByEliminadoFalse();
}