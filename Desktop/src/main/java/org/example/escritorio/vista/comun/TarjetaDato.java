package org.example.escritorio.vista.comun;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

/**
 * Tarjeta con un número grande y su nombre arriba ("Cobrado hoy  $ 412.350").
 * La destacada va en café con letras blancas; a las demás se les puede cambiar el color del número.
 */
public class TarjetaDato extends Estilo.Tarjeta {

    private final JLabel lblValor = new JLabel("—");

    public TarjetaDato(String nombre, boolean destacada) {
        super(new BorderLayout(), 20);
        setBorder(Estilo.relleno(14, 22, 14, 22));
        if (destacada) {
            setBackground(Estilo.CAFE);
        }

        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(Estilo.fuente(Font.PLAIN, 15f));
        lblNombre.setForeground(destacada ? new Color(255, 255, 255, 220) : Estilo.TEXTO_SUAVE);

        lblValor.setFont(Estilo.fuente(Font.BOLD, 28f));
        lblValor.setForeground(destacada ? Color.WHITE : Estilo.TEXTO);
        // margen para que la última cifra en negrita no quede recortada
        lblValor.setBorder(Estilo.relleno(0, 0, 0, 6));

        javax.swing.JPanel textos = Estilo.transparente(null);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(lblNombre);
        textos.add(Box.createVerticalStrut(6));
        textos.add(lblValor);
        add(textos, BorderLayout.CENTER);
    }

    public void setValor(String valor) {
        lblValor.setText(valor);
    }

    public void setColorValor(Color color) {
        lblValor.setForeground(color);
    }

    @Override
    protected void paintBorder(java.awt.Graphics g) {
        if (getBackground().equals(Estilo.BLANCO)) {
            super.paintBorder(g); // la destacada no lleva borde
        }
    }
}
