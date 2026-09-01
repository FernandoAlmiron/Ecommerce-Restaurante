package org.example.repository.facturacion;

import org.example.Modelo.facturacion.Facturacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacturacionRepository extends JpaRepository<Facturacion, Integer> {
}
