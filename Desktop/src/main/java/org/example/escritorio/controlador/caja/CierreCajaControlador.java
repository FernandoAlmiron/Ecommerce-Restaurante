package org.example.escritorio.controlador.caja;

import org.example.Modelo.enums.EstadoPago;
import org.example.Modelo.enums.MetodoPago;
import org.example.Modelo.enums.OrigenTicket;
import org.example.Modelo.facturacion.CierreCaja;
import org.example.dto.CierreCajaDTO;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.caja.CierreCajaPanel;
import org.example.escritorio.vista.caja.CierreFila;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.comun.Tarea;
import org.example.repository.facturacion.FacturacionRepository;
import org.example.service.facturacion.CierreCajaService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Lógica del cierre de caja (RF-47) con CierreCajaService.
 * Lo esperado en efectivo y la diferencia los calcula el backend; la cajera es el usuario logueado.
 * Tarjeta y QR del día se suman con la misma consulta que usa el backend para el efectivo
 * (FacturacionRepository.sumarPagos: facturaciones de salón pagadas esa fecha).
 */
public class CierreCajaControlador {

    private final CierreCajaPanel vista;
    private final CierreCajaService cierreCajaService;
    private final FacturacionRepository facturacionRepository;

    public CierreCajaControlador(CierreCajaPanel vista) {
        this.vista = vista;
        this.cierreCajaService = Contexto.get(CierreCajaService.class);
        this.facturacionRepository = Contexto.get(FacturacionRepository.class);
    }

    /** Lo que muestra la pantalla: lo cobrado hoy con tarjeta y QR, y los cierres anteriores. */
    private record Datos(BigDecimal tarjetaHoy, BigDecimal qrHoy, List<CierreFila> cierres) {
    }

    /** Lo cobrado hoy con tarjeta y QR, y los cierres anteriores del restaurante del empleado. */
    public void cargar() {
        int restaurante = Sesion.nroRestaurante();
        LocalDate hoy = LocalDate.now();
        Tarea.ejecutar(vista.getBotonActualizar(),
                () -> new Datos(
                        cobrado(restaurante, hoy, MetodoPago.TARJETA),
                        cobrado(restaurante, hoy, MetodoPago.QR),
                        cierreCajaService.listarTodos().stream()
                                .filter(c -> c.getRestaurante() != null
                                        && c.getRestaurante().getNroRestaurante() == restaurante)
                                .map(c -> conTotales(c, restaurante))
                                .toList()),
                datos -> {
                    vista.mostrarCobradoHoy(datos.tarjetaHoy(), datos.qrHoy());
                    vista.mostrarCierres(datos.cierres());
                });
    }

    public void cerrarCaja() {
        BigDecimal contado = vista.getMontoContado();
        if (contado == null) {
            vista.mostrarError("Escribí el monto contado con números, por ejemplo 150000,00.");
            return;
        }
        if (!vista.confirmar("¿Cerrar la caja de hoy con " + Formatos.pesos(contado) + " contados en efectivo?")) {
            return;
        }

        int restaurante = Sesion.nroRestaurante();
        CierreCajaDTO datos = new CierreCajaDTO();
        datos.setNroRestaurante(restaurante);
        datos.setMontoContado(contado);
        datos.setObservaciones(vista.getObservaciones().isEmpty() ? null : vista.getObservaciones());

        Tarea.ejecutar(vista.getBotonCerrar(),
                () -> conTotales(cierreCajaService.crearCierre(datos, Sesion.usuario()), restaurante),
                fila -> {
                    vista.mostrarResultado(fila);
                    cargar();
                });
    }

    /** Suma lo cobrado en salón con tarjeta y con QR el día del cierre. Corre dentro de la Tarea. */
    private CierreFila conTotales(CierreCaja cierre, int restaurante) {
        return new CierreFila(cierre,
                cobrado(restaurante, cierre.getFecha(), MetodoPago.TARJETA),
                cobrado(restaurante, cierre.getFecha(), MetodoPago.QR));
    }

    private BigDecimal cobrado(int restaurante, LocalDate fecha, MetodoPago metodo) {
        BigDecimal total = facturacionRepository.sumarPagos(restaurante,
                fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay(),
                OrigenTicket.SALON, EstadoPago.PAGADO, metodo);
        return total == null ? BigDecimal.ZERO : total; // null si no hubo pagos con ese método
    }
}