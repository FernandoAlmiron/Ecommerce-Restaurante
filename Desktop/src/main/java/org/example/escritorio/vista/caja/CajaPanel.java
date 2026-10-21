package org.example.escritorio.vista.caja;

import org.example.escritorio.vista.comun.PantallaConSolapas;

import java.awt.Color;

/**
 * Área de Caja: tres solapas, cada una con su controlador.
 * Cobros (RF-40 a RF-45), Cierre de caja (RF-47) y Reporte de delivery (RF-48).
 */
public class CajaPanel extends PantallaConSolapas {

    public CajaPanel() {
        super("Caja");

        CobrosPanel cobros = new CobrosPanel();
        Solapa solapaCobros = agregar("Cobros", cobros);
        agregar("Cierre de caja", new CierreCajaPanel());
        agregar("Reporte de delivery", new ReporteDeliveryPanel());

        // el contador de Cobros sigue a los tickets por cobrar
        cobros.addPropertyChangeListener(CobrosPanel.PROP_POR_COBRAR, e -> {
            int cantidad = (Integer) e.getNewValue();
            solapaCobros.setContador(cantidad > 0 ? String.valueOf(cantidad) : null, null, Color.WHITE);
        });
    }
}
