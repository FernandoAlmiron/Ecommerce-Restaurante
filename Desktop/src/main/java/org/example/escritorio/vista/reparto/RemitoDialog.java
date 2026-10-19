package org.example.escritorio.vista.reparto;

import org.example.dto.RemitoDTO;
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

/**
 * Remito de un envío (RF-39), armado con el RemitoDTO del backend: cliente, dirección, platos,
 * total, método y estado de pago. Se muestra como un papel y se puede imprimir.
 */
public class RemitoDialog extends JDialog {

    private static final int ANCHO = 44;

    public RemitoDialog(Window padre, RemitoDTO remito) {
        super(padre, "Remito — Envío N° " + remito.idEnvio(), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTextArea texto = new JTextArea(armarTexto(remito));
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

    private static String armarTexto(RemitoDTO r) {
        StringBuilder sb = new StringBuilder();
        centrado(sb, "REMITO DE ENVÍO");
        linea(sb);
        sb.append("Envío N° ").append(r.idEnvio()).append("   Ticket N° ").append(r.nroTicket()).append('\n');
        sb.append("Cliente:   ").append(r.cliente()).append('\n');
        if (r.nombreReceptor() != null && !r.nombreReceptor().equals(r.cliente())) {
            sb.append("Recibe:    ").append(r.nombreReceptor()).append('\n');
        }
        sb.append("Celular:   ").append(r.celular() == null ? "—" : r.celular()).append('\n');
        sb.append("Dirección: ").append(r.direccionEntrega()).append('\n');
        linea(sb);
        for (RemitoDTO.Item item : r.pedidos()) {
            sb.append(item.cantidad()).append(" x ").append(item.plato()).append('\n');
            if (item.observaciones() != null && !item.observaciones().isBlank()) {
                sb.append("    (").append(item.observaciones()).append(")\n");
            }
        }
        linea(sb);
        columnas(sb, "TOTAL", Formatos.pesos(r.total()));
        columnas(sb, "Pago", EnvioTableModel.pago(r));
        columnas(sb, "Estado del envío", EnvioTableModel.texto(r.estado()));
        columnas(sb, "Repartidor", r.repartidor());
        linea(sb);
        return sb.toString();
    }

    private static void linea(StringBuilder sb) {
        sb.append("-".repeat(ANCHO)).append('\n');
    }

    private static void centrado(StringBuilder sb, String texto) {
        int espacios = Math.max(0, (ANCHO - texto.length()) / 2);
        sb.append(" ".repeat(espacios)).append(texto).append('\n');
    }

    /** Texto a la izquierda y valor a la derecha, en la misma línea. */
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