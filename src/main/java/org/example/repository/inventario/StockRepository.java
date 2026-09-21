package org.example.repository.inventario;

import jakarta.persistence.LockModeType;
import org.example.Modelo.inventario.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.idStock = :id")
    Optional<Stock> findByIdForUpdate(@Param("id") int id);
}
