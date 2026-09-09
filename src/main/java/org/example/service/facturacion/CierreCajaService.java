package org.example.service.facturacion;

import org.example.Modelo.facturacion.CierreCaja;
import org.example.repository.facturacion.CierreCajaRepository;
import org.example.repository.facturacion.FacturacionRepository;
import org.example.repository.persona.EmpleadoRepository;
import org.example.repository.RestauranteRepository;
import org.example.dto.CierreCajaDTO;
import org.springframework.stereotype.Service;
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

    public CierreCaja crearCierre(CierreCajaDTO dto) {
        LocalDate hoy = LocalDate.now();
        Double esperado = facturacionRepository
                .sumMontoTotalByFechaAndRestaurante(dto.getNroRestaurante(), hoy);
        if (esperado == null) esperado = 0.0;

        CierreCaja cierre = new CierreCaja();
        cierre.setFecha(hoy);
        cierre.setMontoEsperado(esperado);
        cierre.setMontoContado(dto.getMontoContado());
        cierre.setObservaciones(dto.getObservaciones());
        cierre.setCajera(empleadoRepository.findById(dto.getLegajoCajera()).orElseThrow());
        cierre.setRestaurante(restauranteRepository.findById(dto.getNroRestaurante()).orElseThrow());
        cierre.calcularDiferencia();

        return cierreCajaRepository.save(cierre);
    }

    public List<CierreCaja> listarTodos() { return cierreCajaRepository.findAll(); }
    public CierreCaja buscarPorId(int id) { return cierreCajaRepository.findById(id).orElseThrow(); }
}