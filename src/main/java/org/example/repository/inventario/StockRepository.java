package org.example.repository.inventario;

import org.example.Modelo.inventario.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Integer> {
}
