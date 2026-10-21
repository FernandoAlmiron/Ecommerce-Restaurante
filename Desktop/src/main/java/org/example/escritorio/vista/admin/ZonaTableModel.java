package org.example.escritorio.vista.admin;

import org.example.Modelo.reserva.Zona;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.comun.ModeloTablaBase;

/** Columnas de la lista de zonas del salón (RF-51). */
public class ZonaTableModel extends ModeloTablaBase<Zona> {

    public ZonaTableModel() {
        super("Nombre", "Capacidad (personas)", "Activa");
    }

    @Override
    protected Object valor(Zona z, int columna) {
        return switch (columna) {
            case 0 -> z.getNombre();
            case 1 -> z.getCapacidadMaxima();
            case 2 -> Formatos.siNo(z.isActiva());
            default -> null;
        };
    }

    @Override
    protected Class<?> tipo(int columna) {
        return columna == 1 ? Integer.class : String.class;
    }
}