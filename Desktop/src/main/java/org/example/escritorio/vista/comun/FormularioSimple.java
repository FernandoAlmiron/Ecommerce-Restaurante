package org.example.escritorio.vista.comun;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Formulario de una columna con la estética "La Nona": cada campo con su etiqueta arriba.
 * Lo usan los formularios chicos (zona, sector, proveedor...) que se abren con FormularioDialog
 * o que se muestran dentro de una pantalla (por ejemplo, la categoría elegida).
 *
 *   new FormularioSimple("Nueva zona").fila("Nombre", txtNombre).fila("Capacidad", spnCapacidad);
 *
 * Los campos de texto, números y listas se estilizan solos. Una casilla con etiqueta vacía va en una cajita
 * crema con su explicación (la nota que se agregue justo después).
 */
public class FormularioSimple extends JPanel {

    private final String titulo;
    private final GridBagConstraints c = new GridBagConstraints();
    private int y;
    /** Si lo último que se agregó fue una casilla sin etiqueta, la casilla y su caja (la nota va adentro). */
    private JCheckBox ultimaCasilla;
    private JComponent ultimaCaja;

    public FormularioSimple(String titulo) {
        super(new GridBagLayout());
        this.titulo = titulo;
        setOpaque(false);
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
    }

    /** Lo que identifica al formulario ("Nueva zona", el nombre de lo que se modifica...). */
    public String getTitulo() {
        return titulo;
    }

    /** Agrega un campo con su etiqueta arriba. Con etiqueta vacía y una casilla, va en una cajita crema. */
    public FormularioSimple fila(String etiqueta, JComponent campo) {
        ultimaCasilla = null;
        estilizar(campo);
        if ((etiqueta == null || etiqueta.isBlank()) && campo instanceof JCheckBox casilla) {
            ultimaCaja = Formulario.opcion(casilla, null);
            agregar(ultimaCaja);
            ultimaCasilla = casilla;
            return this;
        }
        agregar(etiqueta == null || etiqueta.isBlank() ? campo : Formulario.campo(etiqueta, campo, null));
        return this;
    }

    /** Texto de ayuda en gris. Si va justo después de una casilla, queda como su explicación. */
    public FormularioSimple nota(String texto) {
        if (ultimaCasilla != null) {
            // se rearma la caja de la casilla, ahora con su explicación, en el mismo lugar
            GridBagConstraints lugar = ((GridBagLayout) getLayout()).getConstraints(ultimaCaja);
            remove(ultimaCaja);
            ultimaCaja = Formulario.opcion(ultimaCasilla, conAncho(texto, 420));
            add(ultimaCaja, lugar);
            ultimaCasilla = null;
            return this;
        }
        agregar(Formulario.nota(new JLabel(conAncho(texto, 470))));
        return this;
    }

    /** Texto que salta de renglón en vez de estirar la ventana. */
    private static String conAncho(String texto, int ancho) {
        return "<html><div style='width:" + ancho + "px'>" + texto + "</div></html>";
    }

    private void agregar(JComponent componente) {
        c.gridy = y;
        c.insets = new Insets(y == 0 ? 0 : 18, 0, 0, 0);
        add(componente, c);
        y++;
    }

    /** Cada tipo de entrada con su estilo; lo que no se reconoce queda como está. */
    private static void estilizar(JComponent campo) {
        if (campo instanceof JTextField texto) {
            Formulario.estilizar(texto, null);
        } else if (campo instanceof JSpinner spinner) {
            Formulario.estilizar(spinner);
        } else if (campo instanceof JComboBox<?> combo) {
            Formulario.estilizar(combo);
        }
    }
}
