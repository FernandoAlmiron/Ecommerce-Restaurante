package org.example.escritorio.controlador.caja;

import org.example.Modelo.enums.EstadoPago;
import org.example.Modelo.enums.MetodoPago;
import org.example.Modelo.enums.OrigenTicket;
import org.example.Modelo.facturacion.CierreCaja;
import org.example.dto.CierreCajaDTO;
import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.Transaccion;
import org.example.escritorio.vista.caja.CierreCajaPanel;
import org.example.escritorio.vista.caja.CierreDetalleDialog;
import org.example.escritorio.vista.caja.CierreFila;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.comun.Tarea;
import org.example.repository.facturacion.FacturacionRepository;
import org.example.service.facturacion.CierreCajaService;
import org.example.service.facturacion.FacturacionService;

import javax.swing.SwingUtilities;
import java.util.Comparator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Lógica del cierre de caja (RF-47) con CierreCajaService.
 * Lo esperado en efectivo y la diferencia que quedan guardados los calcula el backend; la cajera es el
 * usuario logueado. Antes de cerrar, la pantalla muestra lo que se cobró hoy en efectivo, tarjeta y QR con
 * la misma consulta que usa el backend (FacturacionRepository.sumarPagos: facturaciones de salón pagadas
 * esa fecha), así la cajera sabe cuánto tiene que haber en la caja.
 */
public class CierreCajaControlador {

    private final CierreCajaPanel vista;
    private final CierreCajaService cierreCajaService;
    private final FacturacionRepository facturacionRepository;
    private final FacturacionService facturacionService;

    public CierreCajaControlador(CierreCajaPanel vista) {
        this.vista = vista;
        this.cierreCajaService = Contexto.get(CierreCajaService.class);
        this.facturacionRepository = Contexto.get(FacturacionRepository.class);
        this.facturacionService = Contexto.get(FacturacionService.class);
    }

    /**
     * Abre el detalle de un cierre anterior: el arqueo y los cobros de salón pagados ese día
     * (los mismos que suma el cierre: consumo + propina, por fecha de pago).
     */
    public void verDetalle(CierreFila fila) {
        int restaurante = Sesion.nroRestaurante();
        LocalDate dia = fila.cierre().getFecha();
        Tarea.ejecutar(vista.getBotonDetalle(),
                // el ticket de cada facturación es lazy: se lee dentro de la transacción
                () -> Transaccion.leer(() -> facturacionService.listarTodos().stream()
                        .filter(f -> f.getEstadoPago() == EstadoPago.PAGADO && f.getFechaPago() != null
                                && dia.equals(f.getFechaPago().toLocalDate()))
                        .filter(f -> f.getTicket() != null && f.getTicket().getOrigen() == OrigenTicket.SALON
                                && f.getTicket().getRestaurante() != null
                                && f.getTicket().getRestaurante().getNroRestaurante() == restaurante)
                        .sorted(Comparator.comparing(f -> f.getFechaPago()))
                        .map(f -> new CierreDetalleDialog.Cobro(f.getTicket().getNroTicket(), f.getFechaPago(),
                                f.getMetodoPago(), f.getMontoTotal().add(f.getPropina())))
                        .toList()),
                cobros -> new CierreDetalleDialog(SwingUtilities.getWindowAncestor(vista), fila, cobros).setVisible(true));
    }

    /** Lo que muestra la pantalla: lo cobrado hoy con cada método, y los cierres anteriores. */
    private record Datos(BigDecimal efectivoHoy, BigDecimal tarjetaHoy, BigDecimal qrHoy, List<CierreFila> cierres) {
    }

    /** true mientras hay una consulta en curso, para que el refresco automático no las encime. */
    private boolean cargando;

    /** Lo cobrado hoy en efectivo, tarjeta y QR, y los cierres anteriores del restaurante del empleado. */
    public void cargar() {
        cargar(true);
    }

    /**
     * @param pedidoPorUsuario true si lo pidió el usuario (botón, al abrir, al cerrar la caja): los errores se
     *                         muestran en un diálogo. false para el refresco automático: sin diálogo ni botón ocupado.
     */
    public void cargar(boolean pedidoPorUsuario) {
        if (cargando) {
            return;
        }
        cargando = true;
        int restaurante = Sesion.nroRestaurante();
        LocalDate hoy = LocalDate.now();
        Tarea.ejecutar(pedidoPorUsuario ? vista.getBotonActualizar() : null,
                () -> new Datos(
                        cobrado(restaurante, hoy, MetodoPago.EFECTIVO),
                        cobrado(restaurante, hoy, MetodoPago.TARJETA),
                        cobrado(restaurante, hoy, MetodoPago.QR),
                        cierreCajaService.listarTodos().stream()
                                .filter(c -> c.getRestaurante() != null
                                        && c.getRestaurante().getNroRestaurante() == restaurante)
                                .map(c -> conTotales(c, restaurante))
                                .toList()),
                datos -> {
                    cargando = false;
                    vista.mostrarCobradoHoy(datos.efectivoHoy(), datos.tarjetaHoy(), datos.qrHoy());
                    vista.mostrarCierres(datos.cierres());
                },
                mensaje -> {
                    cargando = false;
                    if (pedidoPorUsuario) {
                        vista.mostrarError(mensaje);
                    }
                });
    }

    public void cerrarCaja() {
        BigDecimal contado = vista.getMontoContado();
        if (contado == null) {
            vista.mostrarError("Escribí el monto contado con números, por ejemplo 150000,00.");
            return;
        }
        BigDecimal esperado = vista.getEfectivoEsperado();
        String pregunta = "¿Cerrar la caja de hoy con " + Formatos.pesos(contado) + " contados en efectivo?";
        if (esperado != null && contado.compareTo(esperado) != 0) {
            BigDecimal diferencia = contado.subtract(esperado);
            pregunta = "Tendría que haber " + Formatos.pesos(esperado) + " en efectivo y contaste "
                    + Formatos.pesos(contado) + ": " + (diferencia.signum() > 0 ? "sobran " : "faltan ")
                    + Formatos.pesos(diferencia.abs()) + ".\n"
                    + (vista.getObservaciones().isEmpty() ? "Conviene anotar el motivo en Observaciones.\n" : "")
                    + "¿Cerrar la caja igual?";
        }
        if (!vista.confirmar(pregunta)) {
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