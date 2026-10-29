package org.example.escritorio.controlador.caja;

import org.example.Modelo.facturacion.Facturacion;
import org.example.Modelo.facturacion.Ticket;
import org.example.Modelo.menu.Pedido;
import org.example.Modelo.reserva.Envio;
import org.example.dto.ComprobanteDTO;
import org.example.escritorio.Contexto;
import org.example.escritorio.Transaccion;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.caja.CobroFila;
import org.example.escritorio.vista.caja.CobrosPanel;
import org.example.escritorio.vista.caja.ComprobanteDialog;
import org.example.escritorio.vista.caja.FacturarDialog;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.comun.Tarea;
import org.example.service.facturacion.FacturacionService;
import org.example.service.facturacion.TicketService;
import org.example.service.reserva.EnvioService;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lógica de cobros con FacturacionService (RF-40 a RF-45). Las reglas están en el backend:
 * una sola facturación por ticket, propina de 5, 10 o 15 %, una facturación pagada no se modifica.
 */
public class CobrosControlador {

    private final CobrosPanel vista;
    private final TicketService ticketService;
    private final FacturacionService facturacionService;
    private final EnvioService envioService;

    public CobrosControlador(CobrosPanel vista) {
        this.vista = vista;
        this.ticketService = Contexto.get(TicketService.class);
        this.facturacionService = Contexto.get(FacturacionService.class);
        this.envioService = Contexto.get(EnvioService.class);
    }

    /**
     * Carga los tickets del restaurante (salón y delivery) con su facturación.
     * @param seleccionar nro de ticket a dejar seleccionado, o null para mantener el actual
     */
    public void cargar(Integer seleccionar) {
        Integer aSeleccionar = seleccionar != null ? seleccionar
                : vista.cobroSeleccionado().map(CobroFila::nroTicket).orElse(null);
        int restaurante = Sesion.nroRestaurante();

        Tarea.ejecutar(vista.getBotonActualizar(), () -> Transaccion.leer(() -> {
            Map<Integer, Envio> envios = envioService.listarTodos().stream()
                    .filter(e -> e.getTicket() != null)
                    .collect(Collectors.toMap(e -> e.getTicket().getNroTicket(), Function.identity(), (a, b) -> a));
            return ticketService.listarTodos().stream()
                    .filter(t -> t.getRestaurante() != null
                            && t.getRestaurante().getNroRestaurante() == restaurante)
                    .map(t -> CobroFila.desde(t, envios.get(t.getNroTicket())))
                    .toList();
        }), cobros -> {
            vista.mostrarCobros(cobros);
            if (aSeleccionar != null) {
                vista.seleccionarTicket(aSeleccionar);
            }
            alCambiarSeleccion();
        });
    }

    /** Muestra los importes (ya vienen con la fila) y pide los pedidos del ticket. */
    public void alCambiarSeleccion() {
        CobroFila cobro = vista.cobroSeleccionado().orElse(null);
        if (cobro == null) {
            vista.mostrarSinSeleccion();
            return;
        }
        vista.mostrarCobro(cobro);

        int nro = cobro.nroTicket();
        Tarea.ejecutar(vista.getTablaPedidos(),
                () -> Transaccion.leer(() -> {
                    List<Pedido> pedidos = ticketService.buscarPorId(nro).getPedidos();
                    return pedidos == null ? List.<Pedido>of() : new ArrayList<>(pedidos);
                }),
                pedidos -> {
                    // si mientras tanto eligieron otro ticket, esta respuesta ya no sirve
                    if (vista.cobroSeleccionado().map(c -> c.nroTicket() == nro).orElse(false)) {
                        vista.mostrarPedidos(pedidos);
                    }
                });
    }

    /**
     * RF-40: FacturacionService.crear calcula total y fecha; solo se le pasa el ticket y el método.
     * Antes de pedir el método se mira en la base si la cocina terminó: la lista puede estar desactualizada
     * (el backend igual no deja facturar con platos en la comanda).
     */
    public void facturar() {
        vista.cobroSeleccionado().filter(CobroFila::sePuedeFacturar).ifPresent(cobro ->
                Tarea.ejecutar(vista.getBotonFacturar(),
                        () -> Transaccion.leer(() -> ticketService.buscarPorId(cobro.nroTicket()).platosEnCocina()),
                        enCocina -> {
                            if (enCocina > 0) {
                                vista.mostrarError("Todavía hay " + enCocina + (enCocina == 1 ? " plato" : " platos")
                                        + " en la comanda: se factura cuando la cocina los entregue.");
                                cargar(cobro.nroTicket());
                                return;
                            }
                            FacturarDialog.pedirMetodo(vista, cobro).ifPresent(metodo -> {
                                Ticket ticket = new Ticket();
                                ticket.setNroTicket(cobro.nroTicket());
                                Facturacion datos = new Facturacion();
                                datos.setTicket(ticket);
                                datos.setMetodoPago(metodo);
                                ejecutar(vista.getBotonFacturar(), cobro, () -> facturacionService.crear(datos));
                            });
                        }));
    }

    /** RF-42. */
    public void aplicarPropina() {
        int porcentaje = vista.getPorcentajePropina();
        vista.cobroSeleccionado().filter(CobroFila::pendiente).ifPresent(cobro ->
                ejecutar(vista.getBotonPropina(), cobro,
                        () -> facturacionService.aplicarPropina(cobro.nroFacturacion(), porcentaje)));
    }

    /** RF-43: vuelve a sumar los pedidos del ticket mientras la facturación esté pendiente. */
    public void recalcular() {
        vista.cobroSeleccionado().filter(CobroFila::pendiente).ifPresent(cobro ->
                ejecutar(vista.getBotonRecalcular(), cobro,
                        () -> facturacionService.calcularTotal(cobro.nroFacturacion())));
    }

    /** RF-44: pasa a PAGADO con la fecha de pago. */
    public void confirmarPago() {
        vista.cobroSeleccionado().filter(CobroFila::pendiente).ifPresent(cobro -> {
            String pregunta = "¿Confirmar el pago de " + Formatos.pesos(cobro.aPagar())
                    + " en " + CobrosPanel.texto(cobro.metodoPago()) + " del ticket N° " + cobro.nroTicket() + "?";
            if (vista.confirmar(pregunta)) {
                ejecutar(vista.getBotonConfirmarPago(), cobro,
                        () -> facturacionService.confirmarPago(cobro.nroFacturacion()));
            }
        });
    }

    /** RF-45: usa el ComprobanteDTO del backend, el mismo que devuelve la API. */
    public void verComprobante() {
        vista.cobroSeleccionado().filter(c -> c.nroFacturacion() != null).ifPresent(cobro ->
                Tarea.ejecutar(vista.getBotonComprobante(),
                        // ComprobanteDTO.desde recorre los pedidos (lazy): va dentro de la transacción
                        () -> Transaccion.leer(() ->
                                ComprobanteDTO.desde(facturacionService.buscarPorId(cobro.nroFacturacion()))),
                        comprobante -> new ComprobanteDialog(SwingUtilities.getWindowAncestor(vista), comprobante)
                                .setVisible(true)));
    }

    /** Ejecuta una acción sobre la facturación y vuelve a cargar dejando el mismo ticket elegido. */
    private void ejecutar(JButton origen, CobroFila cobro, Callable<?> accion) {
        Tarea.ejecutar(origen, accion,
                resultado -> cargar(cobro.nroTicket()),
                mensaje -> {
                    vista.mostrarError(mensaje);
                    cargar(cobro.nroTicket()); // los datos pueden haber cambiado: se vuelven a pedir
                });
    }
}