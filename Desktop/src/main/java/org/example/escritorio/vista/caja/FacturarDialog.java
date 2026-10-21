package org.example.escritorio.vista.caja;

import org.example.Modelo.enums.MetodoPago;
import org.example.escritorio.vista.comun.DialogoFormulario;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formulario;
import org.example.escritorio.vista.comun.Selector;
import org.example.escritorio.vista.comun.Tablas;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.util.Optional;

/**
 * Pide el método de pago para facturar un ticket (RF-40). Solo elige: facturar lo hace el controlador.
 * En un delivery viene elegido el método que puso el cliente al pedir.
 */
public final class FacturarDialog extends DialogoFormulario {

    private static final MetodoPago[] METODOS = MetodoPago.values();

    private final CobroFila cobro;
    private final Selector selMetodo;
    private MetodoPago elegido;

    private FacturarDialog(Window padre, CobroFila cobro) {
        super(padre, "Caja · Ticket N° " + cobro.nroTicket(), "Facturar", cobro.cliente(), "Facturar");
        this.cobro = cobro;

        String[] textos = new String[METODOS.length];
        for (int i = 0; i < METODOS.length; i++) {
            textos[i] = CobrosPanel.texto(METODOS[i]);
        }
        selMetodo = new Selector(textos);
        MetodoPago inicial = cobro.metodoElegidoPorCliente() != null ? cobro.metodoElegidoPorCliente() : MetodoPago.EFECTIVO;
        selMetodo.elegir(inicial.ordinal());

        btnPrincipal.addActionListener(e -> {
            elegido = METODOS[selMetodo.getElegida()];
            dispose();
        });

        armar();
    }

    @Override
    protected JComponent armarCuerpo() {
        JLabel etiqueta = new JLabel("Consumo");
        etiqueta.setFont(Estilo.fuente(Font.PLAIN, 16f));
        etiqueta.setForeground(Estilo.TEXTO_SUAVE);
        JLabel consumo = new JLabel(Tablas.pesosCortos(cobro.consumo()) + "  ·  " + cobro.cantidadPedidos()
                + (cobro.cantidadPedidos() == 1 ? " pedido" : " pedidos"));
        consumo.setFont(Estilo.fuente(Font.BOLD, 18f));
        consumo.setForeground(Estilo.TEXTO);
        Estilo.Tarjeta caja = new Estilo.Tarjeta(new BorderLayout(), 14);
        caja.setBackground(Estilo.FILA_SELECCIONADA);
        caja.setBorder(Estilo.relleno(14, 18, 14, 18));
        caja.add(etiqueta, BorderLayout.WEST);
        caja.add(consumo, BorderLayout.EAST);

        JPanel metodo = Estilo.transparente(new BorderLayout());
        metodo.add(selMetodo, BorderLayout.WEST);
        String ayuda = cobro.metodoElegidoPorCliente() != null
                ? "El cliente eligió " + CobrosPanel.texto(cobro.metodoElegidoPorCliente()) + " al pedir."
                : "La propina se puede aplicar después, antes de confirmar el pago.";
        return Formulario.cuerpo(caja, Formulario.campo("Método de pago", metodo, ayuda));
    }

    /** Abre la ventana y devuelve el método elegido, o vacío si se canceló. */
    public static Optional<MetodoPago> pedirMetodo(Component padre, CobroFila cobro) {
        Window ventana = padre instanceof Window w ? w : SwingUtilities.getWindowAncestor(padre);
        FacturarDialog dialogo = new FacturarDialog(ventana, cobro);
        dialogo.setVisible(true);
        return Optional.ofNullable(dialogo.elegido);
    }
}
