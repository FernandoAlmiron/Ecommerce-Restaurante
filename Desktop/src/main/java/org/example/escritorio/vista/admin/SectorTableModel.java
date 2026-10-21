package org.example.escritorio.vista.admin;

import org.example.Modelo.persona.Sector;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.comun.ModeloTablaBase;

/** Columnas de la lista de sectores del restaurante (RF-52). */
public class SectorTableModel extends ModeloTablaBase<Sector> {

    public SectorTableModel() {
        super("Sector", "Activo");
    }

    @Override
    protected Object valor(Sector s, int columna) {
        return switch (columna) {
            case 0 -> s.getNombre();
            case 1 -> Formatos.siNo(s.isActiva());
            default -> null;
        };
    }
}