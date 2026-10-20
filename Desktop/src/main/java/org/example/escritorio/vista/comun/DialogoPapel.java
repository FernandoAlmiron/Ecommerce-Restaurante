package org.example.escritorio.vista.comun;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.print.PrinterException;

/**
 * Ventana que muestra un texto como un papel impreso (comprobante, remito...) y lo puede imprimir.
 * Cada papel concreto solo arma su texto con {@link #armarTexto()}; acá hay ayudas para las columnas.
 */
public abstract class DialogoPapel extends DialogoFormulario {

    /** Caracteres por línea, como un ticket de impresora. */
    protected static final int ANCHO = 44;

    private final JTextArea texto = new JTextArea();

    protected DialogoPapel(Window padre, String seccion, String titulo, String subtitulo) {
        super(padre, seccion, titulo, subtitulo, "Imprimir");
        btnCancelar.setText("Cerrar");
        btnPrincipal.addActionListener(e -> imprimir());
    }

    /** El contenido del papel. Se llama una sola vez, al armar la ventana. */
    protected abstract String armarTexto();

    @Override
    protected final JComponent armarCuerpo() {
        texto.setText(armarTexto());
        texto.setEditable(false);
        texto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        texto.setForeground(Estilo.TEXTO);
        texto.setBackground(Estilo.BLANCO);
        texto.setBorder(Estilo.relleno(16, 20, 16, 20));
        texto.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(texto);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        Estilo.Tarjeta papel = new Estilo.Tarjeta(new BorderLayout(), 14);
        papel.setBorder(Estilo.relleno(1, 1, 1, 1));
        papel.add(scroll, BorderLayout.CENTER);
        return papel;
    }

    /** Arma la ventana y la deja de un alto razonable aunque el papel sea largo. */
    protected final void armarPapel() {
        armar();
        setSize(getWidth(), Math.min(getHeight(), 720));
        setLocationRelativeTo(getOwner());
    }

    // ---------- ayudas para armar el texto ----------

    protected static void linea(StringBuilder sb) {
        sb.append("-".repeat(ANCHO)).append('\n');
    }

    protected static void centrado(StringBuilder sb, String texto) {
        int espacios = Math.max(0, (ANCHO - texto.length()) / 2);
        sb.append(" ".repeat(espacios)).append(texto).append('\n');
    }

    /** Texto a la izquierda y valor a la derecha, en la misma línea. */
    protected static void columnas(StringBuilder sb, String izquierda, String derecha) {
        int espacios = Math.max(1, ANCHO - izquierda.length() - derecha.length());
        sb.append(izquierda).append(" ".repeat(espacios)).append(derecha).append('\n');
    }

    private void imprimir() {
        try {
            texto.print(); // abre el diálogo de impresión del sistema
        } catch (PrinterException e) {
            Dialogos.error(this, "No se pudo imprimir: " + e.getMessage());
        }
    }
}
