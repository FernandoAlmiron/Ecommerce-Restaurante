package org.example.escritorio.controlador.caja;

import org.example.escritorio.Contexto;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.caja.ReporteDeliveryPanel;
import org.example.escritorio.vista.comun.Tarea;
import org.example.service.facturacion.FacturacionService;

import java.time.LocalDate;

/** Lógica del reporte de delivery (RF-48) con FacturacionService.reporteDelivery. */
public class ReporteDeliveryControlador {

    private final ReporteDeliveryPanel vista;
    private final FacturacionService facturacionService;

    public ReporteDeliveryControlador(ReporteDeliveryPanel vista) {
        this.vista = vista;
        this.facturacionService = Contexto.get(FacturacionService.class);
    }

    public void verReporte() {
        LocalDate fecha = vista.getFecha();
        int restaurante = Sesion.nroRestaurante();
        Tarea.ejecutar(vista.getBotonVer(),
                () -> facturacionService.reporteDelivery(restaurante, fecha),
                totales -> vista.mostrarReporte(fecha, totales));
    }
}