package org.example.escritorio.vista.reparto;

import org.example.Modelo.enums.EstadoEnvio;
import org.example.dto.RemitoDTO;
import org.example.escritorio.vista.caja.CobrosPanel;
import org.example.escritorio.vista.comun.ModeloTablaBase;

import java.math.BigDecimal;

/** Columnas de la lista de envíos. Cada fila es el RemitoDTO del backend. */
public class EnvioTableModel extends ModeloTablaBase<RemitoDTO> {

    public static final int COL_ENVIO = 0;
    public static final int COL_ESTADO = 7;

    public EnvioTableModel() {
        super("Envío", "Ticket", "Recibe", "Dirección", "Platos", "Total", "Pago", "Estado", "Repartidor");
    }

    @Override
    protected Object valor(RemitoDTO r, int columna) {
        return switch (columna) {
            case 0 -> r.idEnvio();
            case 1 -> r.nroTicket();
            case 2 -> r.nombreReceptor() != null ? r.nombreReceptor() : r.cliente();
            case 3 -> r.direccionEntrega();
            case 4 -> r.pedidos().stream().mapToInt(RemitoDTO.Item::cantidad).sum();
            case 5 -> r.total();
            case 6 -> pago(r);
            case 7 -> r.estado();
            case 8 -> r.repartidor();
            default -> null;
        };
    }

    @Override
    protected Class<?> tipo(int columna) {
        return switch (columna) {
            case 0, 1, 4 -> Integer.class;
            case 5 -> BigDecimal.class;
            case 7 -> EstadoEnvio.class;
            default -> String.class;
        };
    }

    /** "Efectivo · cobrar" si el repartidor tiene que cobrar al entregar; si no, el método y "pagado". */
    public static String pago(RemitoDTO r) {
        String metodo = r.metodoPago() == null ? "—" : CobrosPanel.texto(r.metodoPago());
        return "PAGADO".equals(r.estadoPago()) ? metodo + " · pagado" : metodo + " · cobrar";
    }

    /** PENDIENTE → Pendiente, EN_CAMINO → En camino... */
    public static String texto(EstadoEnvio estado) {
        return switch (estado) {
            case PENDIENTE -> "Pendiente";
            case EN_CAMINO -> "En camino";
            case ENTREGADO -> "Entregado";
            case CANCELADO -> "Cancelado";
        };
    }
}