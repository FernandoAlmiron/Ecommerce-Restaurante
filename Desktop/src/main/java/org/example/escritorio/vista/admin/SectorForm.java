package org.example.escritorio.vista.admin;

import org.example.Modelo.persona.Sector;
import org.example.escritorio.vista.comun.FormularioSimple;

import javax.swing.JTextField;

/** Campos de un sector: solo el nombre. Activar o desactivar se hace con su propio botón. */
public class SectorForm extends FormularioSimple {

    private final JTextField txtNombre = new JTextField(22);

    public SectorForm(Sector sector) {
        super(sector == null ? "Nuevo sector" : sector.getNombre());
        fila("Nombre", txtNombre);
        nota("El nombre del sector define qué pantallas ven sus empleados al iniciar sesión "
                + "(por ejemplo CAJA, COCINA o RECEPCION): un nombre que el sistema no conoce no da acceso a ninguna.");
        if (sector != null) {
            txtNombre.setText(sector.getNombre());
        }
    }

    public String getNombre() { return txtNombre.getText().trim(); }
}