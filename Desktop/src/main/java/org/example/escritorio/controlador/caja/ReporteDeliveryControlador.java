package org.example.escritorio.controlador.caja;

import org.example.Modelo.enums.EstadoPago;
import org.example.Modelo.enums.OrigenTicket;
import org.example.Modelo.reserva.Envio;
import org.example.escritorio.Contexto;
import org.example.escritorio.Transaccion;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.caja.DeliveryFila;
import org.example.escritorio.vista.caja.ReporteDeliveryPanel;
import org.example.escritorio.vista.comun.Tarea;
import org.example.service.facturacion.FacturacionService;
import org.example.service.reserva.EnvioService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lógica del reporte de delivery (RF-48).
 * Los totales por método los da FacturacionService.reporteDelivery; la lista de pedidos del día se arma acá
 * con el mismo criterio que usa el backend: facturaciones de delivery del restaurante, pagadas ese día.
 */
public class ReporteDeliveryControlador {

    private final ReporteDeliveryPanel vista;
    private final FacturacionService facturacionService;
    private final EnvioService envioService;

    public ReporteDeliveryControlador(ReporteDeliveryPanel vista) {
        this.vista = vista;
        this.facturacionService = Contexto.get(FacturacionService.class);
        this.envioService = Contexto.get(EnvioService.class);
    }

    /** El reporte del día: totales por método (del backend) y los pedidos cobrados. */
    private record Datos(Map<String, BigDecimal> totales, List<DeliveryFila> pedidos) {
    }

    public void verReporte() {
        LocalDate fecha = vista.getFecha();
        int restaurante = Sesion.nroRestaurante();
        LocalDateTime desde = fecha.atStartOfDay();
        LocalDateTime hasta = fecha.plusDays(1).atStartOfDay();

        Tarea.ejecutar(vista.getBotonVer(),
                () -> new Datos(
                        facturacionService.reporteDelivery(restaurante, fecha),
                        Transaccion.leer(() -> {
                            Map<Integer, Envio> envios = envioService.listarTodos().stream()
                                    .filter(e -> e.getTicket() != null)
                                    .collect(Collectors.toMap(e -> e.getTicket().getNroTicket(),
                                            Function.identity(), (a, b) -> a));
                            return facturacionService.listarTodos().stream()
                                    .filter(f -> f.getEstadoPago() == EstadoPago.PAGADO)
                                    .filter(f -> f.getFechaPago() != null
                                            && !f.getFechaPago().isBefore(desde) && f.getFechaPago().isBefore(hasta))
                                    .filter(f -> f.getTicket() != null
                                            && f.getTicket().getOrigen() == OrigenTicket.DELIVERY
                                            && f.getTicket().getRestaurante() != null
                                            && f.getTicket().getRestaurante().getNroRestaurante() == restaurante)
                                    .map(f -> DeliveryFila.desde(f, envios.get(f.getTicket().getNroTicket())))
                                    .sorted(Comparator.comparing(DeliveryFila::hora,
                                            Comparator.nullsLast(Comparator.naturalOrder())))
                                    .toList();
                        })),
                datos -> vista.mostrarReporte(fecha, datos.totales(), datos.pedidos()));
    }
}
