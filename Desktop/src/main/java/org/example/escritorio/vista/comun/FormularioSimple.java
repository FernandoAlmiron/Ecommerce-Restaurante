package org.example.escritorio.vista.comun;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Panel de formulario de dos columnas: etiqueta a la izquierda y campo a la derecha.
 * Lo usan los formularios chicos (zona, sector, proveedor...) que se abren con FormularioDialog.
 *
 *   new FormularioSimple("Nueva zona").fila("Nombre", txtNombre).fila("Capacidad", spnCapacidad);
 */
public class FormularioSimple extends JPanel {

    private final GridBagConstraints c = new GridBagConstraints();
    private int y;

    public FormularioSimple(String titulo) {
        super(new GridBagLayout());
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 12);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));
        c.gridx = 0; c.gridy = y++; c.gridwidth = 2;
        add(lblTitulo, c);
        c.gridwidth = 1;
    }

    /** Agrega una fila. Con etiqueta vacía el campo ocupa solo la columna derecha (por ejemplo, un checkbox). */
    public FormularioSimple fila(String etiqueta, JComponent campo) {
        c.gridx = 0; c.gridy = y; c.weightx = 0;
        add(new JLabel(etiqueta), c);
        c.gridx = 1; c.weightx = 1;
        add(campo, c);
        y++;
        return this;
    }

    /** Texto de ayuda en gris debajo de los campos. */
    public FormularioSimple nota(String texto) {
        JLabel nota = new JLabel("<html><div style='width:320px'>" + texto + "</div></html>");
        nota.setForeground(Color.GRAY);
        c.gridx = 0; c.gridy = y++; c.gridwidth = 2;
        add(nota, c);
        c.gridwidth = 1;
        return this;
    }
}