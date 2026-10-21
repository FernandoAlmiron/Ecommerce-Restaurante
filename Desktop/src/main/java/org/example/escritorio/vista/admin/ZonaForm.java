package org.example.escritorio.vista.admin;

import org.example.Modelo.reserva.Zona;
import org.example.escritorio.vista.comun.FormularioSimple;

import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.text.ParseException;

/** Campos de una zona: nombre, capacidad y si está activa. Con zona null es un alta. */
public class ZonaForm extends FormularioSimple {

    private final JTextField txtNombre = new JTextField(22);
    private final JSpinner spnCapacidad = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
    private final JCheckBox chkActiva = new JCheckBox("Activa (se ofrece al reservar)", true);

    public ZonaForm(Zona zona) {
        super(zona == null ? "Nueva zona" : zona.getNombre());
        fila("Nombre", txtNombre);
        fila("Capacidad (personas)", spnCapacidad);
        fila("", chkActiva);
        nota("Una zona inactiva no se borra: deja de aparecer para reservas nuevas.");

        if (zona != null) {
            txtNombre.setText(zona.getNombre());
            spnCapacidad.setValue(Math.max(1, zona.getCapacidadMaxima()));
            chkActiva.setSelected(zona.isActiva());
        }
    }

    public String getNombre() { return txtNombre.getText().trim(); }
    public boolean isActiva() { return chkActiva.isSelected(); }

    /** null si lo escrito no es un número válido. */
    public Integer getCapacidad() {
        try {
            spnCapacidad.commitEdit();
        } catch (ParseException e) {
            return null;
        }
        return (Integer) spnCapacidad.getValue();
    }
}