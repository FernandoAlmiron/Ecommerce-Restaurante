package org.example.service.facturacion;

import org.example.Modelo.enums.EstadoEnvio;
import org.example.Modelo.enums.EstadoPago;
import org.example.Modelo.enums.OrigenTicket;
import org.example.Modelo.facturacion.Facturacion;
import org.example.Modelo.facturacion.Ticket;
import org.example.repository.facturacion.FacturacionRepository;
import org.example.repository.facturacion.TicketRepository;
import org.example.repository.reserva.EnvioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FacturacionService {

    private final FacturacionRepository facturacionRepository;
    private final TicketRepository ticketRepository;
    private final EnvioRepository envioRepository;

    public FacturacionService(FacturacionRepository facturacionRepository, TicketRepository ticketRepository,
                              EnvioRepository envioRepository) {
        this.facturacionRepository = facturacionRepository;
        this.ticketRepository = ticketRepository;
        this.envioRepository = envioRepository;
    }

    public Facturacion guardar(Facturacion facturacion) {
        return facturacionRepository.save(facturacion);
    }

    // De lo que manda el cliente solo se usa el ticket y el metodo de pago.
    // El total, la fecha, la propina y el estado los calcula el sistema.
    @Transactional
    public Facturacion crear(Facturacion datos) {
        if (datos.getTicket() == null) {
            throw new IllegalArgumentException("La facturacion necesita un ticket");
        }
        if (datos.getMetodoPago() == null) {
            throw new IllegalArgumentException("El metodo de pago es obligatorio");
        }
        Ticket ticket = ticketRepository.findById(datos.getTicket().getNroTicket())
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));

        if (ticket.getFacturacion() != null) {
            throw new IllegalStateException("El ticket ya tiene una facturacion");
        }
        if (ticket.getPedidos() == null || ticket.getPedidos().isEmpty()) {
            throw new IllegalStateException("El ticket no tiene pedidos para facturar");
        }
        // un envio cancelado no se factura
        if (ticket.getOrigen() == OrigenTicket.DELIVERY) {
            envioRepository.findByTicket_NroTicket(ticket.getNroTicket())
                    .filter(envio -> envio.getEstado() == EstadoEnvio.CANCELADO)
                    .ifPresent(envio -> {
                        throw new IllegalStateException("El envio esta cancelado: no se factura");
                    });
        }

        Facturacion nueva = new Facturacion();
        nueva.setTicket(ticket);
        nueva.setMetodoPago(datos.getMetodoPago());
        nueva.setFecha(LocalDateTime.now());
        nueva.calcularTotal();
        return facturacionRepository.save(nueva);
    }

    public Facturacion buscarPorId(int nroFacturacion) {
        return facturacionRepository.findById(nroFacturacion)
                .orElseThrow(() -> new java.util.NoSuchElementException("Facturacion no encontrada"));
    }

    public List<Facturacion> listarTodos() {
        return facturacionRepository.findAll();
    }

    @Transactional
    public BigDecimal calcularTotal(int nroFacturacion) {
        Facturacion facturacion = buscarPorId(nroFacturacion);
        BigDecimal total = facturacion.calcularTotal();
        facturacionRepository.save(facturacion);
        return total;
    }

    public void imprimirTicket(int nroFacturacion) {
        Facturacion facturacion = buscarPorId(nroFacturacion);
        facturacion.imprimirTicket();
    }

    @Transactional
    public Facturacion aplicarPropina(int nroFacturacion, int porcentaje) {
        Facturacion facturacion = buscarPorId(nroFacturacion);
        facturacion.aplicarPropina(porcentaje);
        return facturacionRepository.save(facturacion);
    }

    @Transactional
    public Facturacion confirmarPago(int nroFacturacion) {
        Facturacion facturacion = buscarPorId(nroFacturacion);
        facturacion.confirmarPago();
        return facturacionRepository.save(facturacion);
    }

    public Map<String, BigDecimal> reporteDelivery(int nroRestaurante, LocalDate fecha) {
        List<Object[]> filas = facturacionRepository.resumenPorMetodoPago(
                nroRestaurante, fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay(),
                OrigenTicket.DELIVERY, EstadoPago.PAGADO);
        Map<String, BigDecimal> resultado = new LinkedHashMap<>();
        for (Object[] fila : filas) {
            resultado.put(fila[0].toString(), new BigDecimal(fila[1].toString()));
        }
        return resultado;
    }
}
