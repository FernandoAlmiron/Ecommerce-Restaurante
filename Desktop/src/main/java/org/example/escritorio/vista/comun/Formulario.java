package org.example.escritorio.vista.comun;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * Piezas para armar los campos de un formulario con la estética "La Nona":
 * etiqueta arriba del campo, campos lado a lado, casillas con explicación y el estilo de las entradas.
 * La ventana en sí (encabezado y botones) la pone {@link DialogoFormulario}.
 */
public final class Formulario {

    private Formulario() {
    }

    /** Cuerpo del formulario: los campos uno debajo del otro. */
    public static JPanel cuerpo(JComponent... filas) {
        JPanel cuerpo = Estilo.transparente(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < filas.length; i++) {
            c.gridy = i;
            c.insets = new Insets(i == 0 ? 0 : 18, 0, 0, 0);
            cuerpo.add(filas[i], c);
        }
        return cuerpo;
    }

    /** Etiqueta en negrita arriba del campo y, si hay, una ayuda gris abajo. */
    public static JComponent campo(String etiqueta, JComponent entrada, String ayuda) {
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(Estilo.fuente(Font.BOLD, 15f));
        lbl.setForeground(Estilo.TEXTO);

        JPanel panel = Estilo.transparente(new BorderLayout(0, 8));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(entrada, BorderLayout.CENTER);
        if (ayuda != null) {
            JLabel lblAyuda = new JLabel(ayuda);
            lblAyuda.setFont(Estilo.fuente(Font.PLAIN, 13f));
            lblAyuda.setForeground(Estilo.TEXTO_SUAVE);
            panel.add(lblAyuda, BorderLayout.SOUTH);
        }
        return panel;
    }

    /** Varios campos lado a lado, del mismo ancho. */
    public static JComponent enFila(JComponent... campos) {
        JPanel fila = Estilo.transparente(new GridLayout(1, campos.length, 16, 0));
        for (JComponent c : campos) {
            fila.add(c);
        }
        return fila;
    }

    /** Casilla dentro de una cajita crema, con una explicación debajo del texto. */
    public static JComponent opcion(JCheckBox casilla, String explicacion) {
        casilla.setFont(Estilo.fuente(Font.BOLD, 15f));
        casilla.setForeground(Estilo.TEXTO);
        casilla.setOpaque(false);
        casilla.setIconTextGap(12);
        casilla.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Estilo.casilla(casilla);

        // BorderLayout (y no BoxLayout) para que la explicación tenga todo el ancho de la caja
        JPanel textos = Estilo.transparente(new BorderLayout());
        textos.add(casilla, BorderLayout.NORTH);
        if (explicacion != null) {
            JLabel lbl = new JLabel(explicacion);
            lbl.setFont(Estilo.fuente(Font.PLAIN, 13f));
            lbl.setForeground(Estilo.TEXTO_SUAVE);
            // alineado con el texto de la casilla, no con el cuadradito
            javax.swing.Icon cuadradito = javax.swing.UIManager.getIcon("CheckBox.icon");
            int sangria = (cuadradito != null ? cuadradito.getIconWidth() : 16) + casilla.getIconTextGap()
                    + casilla.getInsets().left;
            lbl.setBorder(Estilo.relleno(2, sangria, 0, 0));
            textos.add(lbl, BorderLayout.CENTER);
        }

        Estilo.Tarjeta caja = new Estilo.Tarjeta(new BorderLayout(), 14);
        caja.setBackground(Estilo.ENCABEZADO_TABLA);
        caja.setBorder(Estilo.relleno(12, 14, 12, 14));
        caja.add(textos, BorderLayout.CENTER);
        return caja;
    }

    /** Texto de ayuda o de estado debajo de un campo (gris; se le puede cambiar el color después). */
    public static JLabel nota(JLabel nota) {
        nota.setFont(Estilo.fuente(Font.PLAIN, 14f));
        nota.setForeground(Estilo.TEXTO_SUAVE);
        return nota;
    }

    // ---------- estilos de las entradas ----------

    public static <T extends JTextField> T estilizar(T campo, String placeholder) {
        campo.setFont(Estilo.fuente(Font.PLAIN, 16f));
        campo.setForeground(Estilo.TEXTO);
        if (placeholder != null) {
            campo.putClientProperty("JTextField.placeholderText", placeholder);
        }
        Estilo.flat(campo, "arc: 14; margin: 10,14,10,14; " + Estilo.bordeCampo(3));
        return campo;
    }

    public static JSpinner estilizar(JSpinner spinner) {
        spinner.setFont(Estilo.fuente(Font.PLAIN, 16f));
        Estilo.flat(spinner, "arc: 14; padding: 10,14,10,10; buttonBackground: " + Estilo.hex(Estilo.ENCABEZADO_TABLA)
                + "; " + Estilo.bordeCampo(3));
        return spinner;
    }

    public static <T> JComboBox<T> estilizar(JComboBox<T> combo) {
        combo.setFont(Estilo.fuente(Font.PLAIN, 16f));
        combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Estilo.flat(combo, "arc: 14; padding: 10,14,10,10; buttonBackground: " + Estilo.hex(Estilo.BLANCO)
                + "; " + Estilo.bordeCampo(3));
        return combo;
    }

}
