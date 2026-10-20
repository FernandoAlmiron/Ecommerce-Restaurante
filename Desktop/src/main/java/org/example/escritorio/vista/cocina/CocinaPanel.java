package org.example.escritorio.vista.cocina;

import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.PantallaConSolapas;

import java.awt.Color;

/**
 * Área de Cocina: tres solapas, cada una es una pantalla con su propio controlador.
 * Comandas (RF-23, RF-24), Menú y recetas (RF-26 a RF-28) y Stock (RF-30, RF-31).
 */
public class CocinaPanel extends PantallaConSolapas {

    public CocinaPanel() {
        super("Cocina");

        ComandasPanel comandas = new ComandasPanel();
        Solapa solapaComandas = agregar("Comandas", comandas);
        agregar("Menú y recetas", new MenuPanel());
        StockPanel stock = new StockPanel();
        Solapa solapaStock = agregar("Stock", stock);

        // los contadores de las solapas siguen a cada pantalla
        comandas.addPropertyChangeListener(ComandasPanel.PROP_CANTIDAD, e -> {
            int cantidad = (Integer) e.getNewValue();
            solapaComandas.setContador(cantidad > 0 ? String.valueOf(cantidad) : null, null, Color.WHITE);
        });
        stock.addPropertyChangeListener(StockPanel.PROP_BAJO_MINIMO, e -> {
            int bajos = (Integer) e.getNewValue();
            solapaStock.setContador(bajos > 0 ? bajos + (bajos == 1 ? " bajo" : " bajos") : null,
                    Estilo.AMARILLO_FONDO, Estilo.AMARILLO_TEXTO);
        });
    }
}
