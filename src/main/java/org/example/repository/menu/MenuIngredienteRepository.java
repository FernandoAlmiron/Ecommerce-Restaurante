package org.example.repository.menu;

import org.example.Modelo.menu.Menu;
import org.example.Modelo.menu.MenuIngrediente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuIngredienteRepository extends JpaRepository<MenuIngrediente, Integer> {
    List<MenuIngrediente> findByMenu(Menu menu);
}
