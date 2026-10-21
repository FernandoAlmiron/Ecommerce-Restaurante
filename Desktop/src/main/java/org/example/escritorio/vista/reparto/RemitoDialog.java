package org.example.escritorio.vista.reparto;

import org.example.dto.RemitoDTO;
import org.example.escritorio.vista.comun.DialogoPapel;
import org.example.escritorio.vista.comun.Formatos;

import java.awt.Window;

/**
 * Remito de un envío (RF-39), armado con el RemitoDTO del backend: cliente, dirección, platos,
 * total, método y estado de pago. Se muestra como un papel y se puede imprimir.
 */
public class RemitoDialog extends DialogoPapel {

    private final RemitoDTO remito;

    public RemitoDialog(Window padre, RemitoDTO remito) {
        super(padre, "Reparto · Ticket " + remito.nroTicket(), "Remito del envío #E-" + remito.idEnvio(),
                remito.direccionEntrega());
        this.remito = remito;
        armarPapel();
    }

    @Override
    protected String armarTexto() {
        return armarTexto(remito);
    }

    private static String armarTexto(RemitoDTO r) {
        StringBuilder sb = new StringBuilder();
        centrado(sb, "REMITO DE ENVÍO");
        linea(sb);
        sb.append("Envío N° ").append(r.idEnvio()).append("   Ticket N° ").append(r.nroTicket()).append('\n');
        sb.append("Cliente:   ").append(r.cliente()).append('\n');
        if (r.nombreReceptor() != null && !r.nombreReceptor().equals(r.cliente())) {
            sb.append("Recibe:    ").append(r.nombreReceptor()).append('\n');
        }
        sb.append("Celular:   ").append(r.celular() == null ? "—" : r.celular()).append('\n');
        sb.append("Dirección: ").append(r.direccionEntrega()).append('\n');
        linea(sb);
        for (RemitoDTO.Item item : r.pedidos()) {
            sb.append(item.cantidad()).append(" x ").append(item.plato()).append('\n');
            if (item.observaciones() != null && !item.observaciones().isBlank()) {
                sb.append("    (").append(item.observaciones()).append(")\n");
            }
        }
        linea(sb);
        columnas(sb, "TOTAL", Formatos.pesos(r.total()));
        columnas(sb, "Pago", EnvioTableModel.pago(r));
        columnas(sb, "Estado del envío", EnvioTableModel.texto(r.estado()));
        columnas(sb, "Repartidor", r.repartidor());
        linea(sb);
        return sb.toString();
    }
}
