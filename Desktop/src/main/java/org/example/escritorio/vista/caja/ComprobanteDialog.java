package org.example.escritorio.vista.caja;

import org.example.Modelo.enums.EstadoPago;
import org.example.dto.ComprobanteDTO;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Formatos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.print.PrinterException;
import java.time.format.DateTimeFormatter;

/**
 * Comprobante de una facturación (RF-45), armado con el ComprobanteDTO del backend.
 * Se muestra como un ticket de papel y se puede imprimir.
 */
public class ComprobanteDialog extends JDialog {

    private static final int ANCHO = 44;
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ComprobanteDialog(Window padre, ComprobanteDTO comprobante) {
        super(padre, "Comprobante — Ticket N° " + comprobante.nroTicket(), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTextArea texto = new JTextArea(armarTexto(comprobante));
        texto.setEditable(false);
        texto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        texto.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        texto.setCaretPosition(0);

        JButton btnImprimir = new JButton("Imprimir");
        JButton btnCerrar = new JButton("Cerrar");
        btnImprimir.addActionListener(e -> imprimir(texto));
        btnCerrar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnImprimir);
        botones.add(btnCerrar);
        getRootPane().setDefaultButton(btnCerrar);

        JPanel raiz = new JPanel(new BorderLayout(0, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        raiz.add(new JScrollPane(texto), BorderLayout.CENTER);
        raiz.add(botones, BorderLayout.SOUTH);
        setContentPane(raiz);

        pack();
        setSize(getWidth(), Math.min(getHeight(), 640));
        setLocationRelativeTo(padre);
    }

    private static String armarTexto(ComprobanteDTO c) {
        StringBuilder sb = new StringBuilder();
        centrado(sb, c.restaurante());
        if (c.direccion() != null) {
            centrado(sb, c.direccion());
        }
        linea(sb);
        sb.append("Comprobante N° ").append(c.nroFacturacion())
                .append("   Ticket N° ").append(c.nroTicket()).append('\n');
        if (c.fecha() != null) {
            sb.append("Fecha: ").append(FECHA_HORA.format(c.fecha())).append('\n');
        }
        linea(sb);
        for (ComprobanteDTO.Item item : c.detalle()) {
            sb.append(item.plato()).append('\n');
            columnas(sb, "  " + item.cantidad() + " x " + Formatos.pesos(item.precioUnitario()),
                    Formatos.pesos(item.subtotal()));
        }
        linea(sb);
        columnas(sb, "Subtotal", Formatos.pesos(c.subtotal()));
        if (c.porcentajePropina() != null) {
            columnas(sb, "Propina " + c.porcentajePropina() + " %", Formatos.pesos(c.propina()));
        }
        columnas(sb, "TOTAL", Formatos.pesos(c.total()));
        linea(sb);
        columnas(sb, "Método de pago", c.metodoPago() == null ? "—" : CobrosPanel.texto(c.metodoPago()));
        columnas(sb, "Estado", c.estadoPago() == EstadoPago.PAGADO ? "PAGADO" : "PENDIENTE DE PAGO");
        if (c.fechaPago() != null) {
            columnas(sb, "Pagado el", FECHA_HORA.format(c.fechaPago()));
        }
        linea(sb);
        centrado(sb, "¡Gracias por su visita!");
        return sb.toString();
    }

    private static void linea(StringBuilder sb) {
        sb.append("-".repeat(ANCHO)).append('\n');
    }

    private static void centrado(StringBuilder sb, String texto) {
        int espacios = Math.max(0, (ANCHO - texto.length()) / 2);
        sb.append(" ".repeat(espacios)).append(texto).append('\n');
    }

    /** Texto a la izquierda e importe a la derecha, en la misma línea. */
    private static void columnas(StringBuilder sb, String izquierda, String derecha) {
        int espacios = Math.max(1, ANCHO - izquierda.length() - derecha.length());
        sb.append(izquierda).append(" ".repeat(espacios)).append(derecha).append('\n');
    }

    private void imprimir(JTextArea texto) {
        try {
            texto.print(); // abre el diálogo de impresión del sistema
        } catch (PrinterException e) {
            Dialogos.error(this, "No se pudo imprimir: " + e.getMessage());
        }
    }
}