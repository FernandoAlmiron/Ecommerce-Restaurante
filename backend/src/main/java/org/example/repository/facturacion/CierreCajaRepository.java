package org.example.repository.facturacion;

import org.example.Modelo.facturacion.CierreCaja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface CierreCajaRepository extends JpaRepository<CierreCaja, Integer> {

    boolean existsByRestaurante_NroRestauranteAndFecha(int nroRestaurante, LocalDate fecha);
}
