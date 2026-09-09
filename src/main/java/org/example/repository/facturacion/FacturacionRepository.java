package org.example.repository.facturacion;

import org.example.Modelo.facturacion.Facturacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface FacturacionRepository extends JpaRepository<Facturacion, Integer> {
    @Query("SELECT COALESCE(SUM(f.montoTotal + f.propina), 0) FROM Facturacion f " +
            "WHERE f.ticket.restaurante.nroRestaurante = :nroRestaurante " +
            "AND FUNCTION('DATE', f.fecha) = :fecha " +
            "AND f.ticket.origen = 'SALON' " +
            "AND f.estadoPago = 'PAGADO'")
    Double sumMontoTotalByFechaAndRestaurante(@Param("nroRestaurante") int nroRestaurante,
                                              @Param("fecha") LocalDate fecha);

    @Query("SELECT f.metodoPago, COALESCE(SUM(f.montoTotal + f.propina), 0) FROM Facturacion f " +
            "WHERE f.ticket.restaurante.nroRestaurante = :nroRestaurante " +
            "AND FUNCTION('DATE', f.fecha) = :fecha " +
            "AND f.ticket.origen = 'DELIVERY' " +
            "AND f.estadoPago = 'PAGADO' " +
            "GROUP BY f.metodoPago")
    List<Object[]> resumenDeliveryPorMetodoPago(@Param("nroRestaurante") int nroRestaurante,
                                                @Param("fecha") LocalDate fecha);
}
