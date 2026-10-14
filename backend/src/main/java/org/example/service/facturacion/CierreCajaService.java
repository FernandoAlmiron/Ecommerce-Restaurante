package org.example.service.facturacion;

import org.example.Modelo.Restaurante;
import org.example.Modelo.enums.EstadoPago;
import org.example.Modelo.enums.MetodoPago;
import org.example.Modelo.enums.OrigenTicket;
import org.example.Modelo.facturacion.CierreCaja;
import org.example.Modelo.persona.Empleado;
import org.example.dto.CierreCajaDTO;
import org.example.repository.RestauranteRepository;
import org.example.repository.facturacion.CierreCajaRepository;
import org.example.repository.facturacion.FacturacionRepository;
import org.example.repository.persona.EmpleadoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CierreCajaService {
    private final CierreCajaRepository cierreCajaRepository;
    private final FacturacionRepository facturacionRepository;
    private final EmpleadoRepository empleadoRepository;
    private final RestauranteRepository restauranteRepository;

    public CierreCajaService(CierreCajaRepository cierreCajaRepository, FacturacionRepository facturacionRepository,
                             EmpleadoRepository empleadoRepository, RestauranteRepository restauranteRepository) {
        this.cierreCajaRepository = cierreCajaRepository;
        this.facturacionRepository = facturacionRepository;
        this.empleadoRepository = empleadoRepository;
        this.restauranteRepository = restauranteRepository;
    }

    public CierreCaja crearCierre(CierreCajaDTO dto, String usernameCajera) {
        if (dto.getMontoContado() == null || dto.getMontoContado().signum() < 0) {
            throw new IllegalArgumentException("El monto contado es obligatorio y no puede ser negativo");
        }
        Empleado cajera = empleadoRepository.findByUsername(usernameCajera);
        if (cajera == null) {
            throw new IllegalArgumentException("Empleado no encontrado");
        }
        Restaurante restaurante = restauranteRepository.findById(dto.getNroRestaurante())
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));

        // Lo esperado en la caja: el EFECTIVO cobrado hoy en el salon (con su propina).
        // Tarjeta y QR no pasan por la caja, y el delivery tiene su propio reporte.
        LocalDate hoy = LocalDate.now();
        BigDecimal esperado = facturacionRepository.sumarPagos(
                dto.getNroRestaurante(), hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay(),
                OrigenTicket.SALON, EstadoPago.PAGADO, MetodoPago.EFECTIVO);
        if (esperado == null) esperado = BigDecimal.ZERO;

        CierreCaja cierre = new CierreCaja();
        cierre.setFecha(hoy);
        cierre.setMontoEsperado(esperado);
        cierre.setMontoContado(dto.getMontoContado());
        cierre.setObservaciones(dto.getObservaciones());
        cierre.setCajera(cajera);
        cierre.setRestaurante(restaurante);
        cierre.calcularDiferencia();

        return cierreCajaRepository.save(cierre);
    }

    public List<CierreCaja> listarTodos() { return cierreCajaRepository.findAll(); }

    public CierreCaja buscarPorId(int id) {
        return cierreCajaRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Cierre de caja no encontrado"));
    }
}