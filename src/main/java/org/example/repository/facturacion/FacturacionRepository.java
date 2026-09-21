package org.example.repository.facturacion;

import org.example.Modelo.enums.EstadoPago;
import org.example.Modelo.enums.MetodoPago;
import org.example.Modelo.enums.OrigenTicket;
import org.example.Modelo.facturacion.Facturacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface FacturacionRepository extends JpaRepository<Facturacion, Integer> {

    // Total (consumo + propina) cobrado con un metodo de pago, en un rango de fechas de PAGO.
    // Devuelve null si no hubo ningun pago (el service lo convierte en cero).
    @Query("SELECT SUM(f.montoTotal + f.propina) FROM Facturacion f " +
            "WHERE f.ticket.restaurante.nroRestaurante = :nroRestaurante " +
            "AND f.fechaPago >= :desde AND f.fechaPago < :hasta " +
            "AND f.ticket.origen = :origen " +
            "AND f.estadoPago = :estado " +
            "AND f.metodoPago = :metodo")
    BigDecimal sumarPagos(@Param("nroRestaurante") int nroRestaurante,
                          @Param("desde") LocalDateTime desde,
                          @Param("hasta") LocalDateTime hasta,
                          @Param("origen") OrigenTicket origen,
                          @Param("estado") EstadoPago estado,
                          @Param("metodo") MetodoPago metodo);

    // Total cobrado agrupado por metodo de pago, en un rango de fechas de PAGO.
    @Query("SELECT f.metodoPago, SUM(f.montoTotal + f.propina) FROM Facturacion f " +
            "WHERE f.ticket.restaurante.nroRestaurante = :nroRestaurante " +
            "AND f.fechaPago >= :desde AND f.fechaPago < :hasta " +
            "AND f.ticket.origen = :origen " +
            "AND f.estadoPago = :estado " +
            "GROUP BY f.metodoPago")
    List<Object[]> resumenPorMetodoPago(@Param("nroRestaurante") int nroRestaurante,
                                        @Param("desde") LocalDateTime desde,
                                        @Param("hasta") LocalDateTime hasta,
                                        @Param("origen") OrigenTicket origen,
                                        @Param("estado") EstadoPago estado);
}
