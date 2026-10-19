package org.example.escritorio.controlador.cocina;

import org.example.dto.ComandaDTO;
import org.example.escritorio.Contexto;
import org.example.escritorio.Transaccion;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.cocina.ComandasPanel;
import org.example.escritorio.vista.comun.Tarea;
import org.example.service.facturacion.TicketService;
import org.example.service.menu.PedidoService;

import java.util.List;

/**
 * Lógica de las comandas. Usa TicketService y el ComandaDTO del backend (el mismo de la API).
 * Muestra los tickets del restaurante que todavía no se facturaron y tienen al menos un pedido.
 */
public class ComandasControlador {

    private final ComandasPanel vista;
    private final TicketService ticketService;
    private final PedidoService pedidoService;

    /** true mientras hay una consulta en curso, para que el refresco automático no las encime. */
    private boolean cargando;

    public ComandasControlador(ComandasPanel vista) {
        this.vista = vista;
        this.ticketService = Contexto.get(TicketService.class);
        this.pedidoService = Contexto.get(PedidoService.class);
    }

    /**
     * @param pedidoPorUsuario true si lo pidió el usuario (botón o al abrir): los errores se muestran en un diálogo.
     *                         false para el refresco automático: el error se avisa arriba, sin diálogo.
     */
    public void cargar(boolean pedidoPorUsuario) {
        if (cargando) {
            return;
        }
        cargando = true;
        int restaurante = Sesion.nroRestaurante();

        Tarea.ejecutar(pedidoPorUsuario ? vista.getBotonActualizar() : null,
                // ComandaDTO.desde recorre los pedidos (lazy): va dentro de la transacción
                () -> Transaccion.leer(() -> ticketService.listarTodos().stream()
                        .filter(t -> t.getFacturacion() == null)
                        .filter(t -> t.getRestaurante() != null
                                && t.getRestaurante().getNroRestaurante() == restaurante)
                        .map(ComandaDTO::desde)
                        .filter(c -> !c.pedidos().isEmpty())
                        .toList()),
                comandas -> {
                    cargando = false;
                    vista.mostrarComandas(comandas);
                    alCambiarSeleccion();
                },
                mensaje -> {
                    cargando = false;
                    vista.mostrarAviso(mensaje);
                    if (pedidoPorUsuario) {
                        vista.mostrarError(mensaje);
                    }
                });
    }

    /**
     * Guarda como listos solo los platos tildados de la comanda elegida. Los que faltan siguen en la comanda;
     * si estaban todos tildados, la comanda sale de la lista.
     */
    public void marcarLista() {
        vista.comandaSeleccionada().ifPresent(comanda -> {
            List<Integer> tildados = vista.pedidosTildadosDeLaComanda();
            if (tildados.isEmpty()) {
                return; // el botón está deshabilitado en ese caso; por las dudas no se manda nada
            }
            int nro = comanda.nroTicket();
            Tarea.ejecutar(vista.getBotonComandaLista(),
                    () -> pedidoService.marcarListos(nro, tildados),
                    marcados -> {
                        vista.platosEnviados(tildados);
                        cargar(true);
                    });
        });
    }

    /** El detalle ya vino con la lista: no hace falta volver a la base. */
    public void alCambiarSeleccion() {
        vista.comandaSeleccionada().ifPresentOrElse(vista::mostrarDetalle, vista::limpiarDetalle);
    }
}