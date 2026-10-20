package org.example.escritorio.vista.caja;

import org.example.Modelo.enums.MetodoPago;
import org.example.Modelo.facturacion.Facturacion;
import org.example.Modelo.reserva.Envio;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Un delivery cobrado, para la lista del reporte de delivery.
 * El total es lo pagado (consumo + propina), igual que lo que suma el backend en el reporte por método.
 */
public record DeliveryFila(
        int nroTicket,
        LocalTime hora,
        String cliente,
        String repartidor,
        MetodoPago metodo,
        BigDecimal total
) {
    /**
     * Llamarlo dentro de Transaccion.leer(...): recorre el ticket de la facturación, que es lazy.
     * @param envio el envío del ticket, o null si no se encontró
     */
    public static DeliveryFila desde(Facturacion f, Envio envio) {
        String cliente = "—";
        String repartidor = "Sin asignar";
        if (envio != null) {
            cliente = envio.getNombreReceptor() != null ? envio.getNombreReceptor()
                    : envio.getCliente().getNombre() + " " + envio.getCliente().getApellido();
            if (envio.getRepartidor() != null) {
                repartidor = envio.getRepartidor().getNombre() + " " + envio.getRepartidor().getApellido();
            }
        }
        return new DeliveryFila(
                f.getTicket().getNroTicket(),
                f.getFechaPago() == null ? null : f.getFechaPago().toLocalTime(),
                cliente,
                repartidor,
                f.getMetodoPago(),
                f.getMontoAPagar());
    }
}
