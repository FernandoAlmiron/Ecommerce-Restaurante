package org.example.escritorio.vista.caja;

import org.example.Modelo.enums.MetodoPago;
import org.example.escritorio.vista.comun.Formatos;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.Optional;

/**
 * Pide el método de pago para facturar un ticket (RF-40). Solo elige: facturar lo hace el controlador.
 * En un delivery viene elegido el método que puso el cliente al pedir.
 */
public final class FacturarDialog extends JDialog {

    private final JComboBox<MetodoPago> cmbMetodo = new JComboBox<>(MetodoPago.values());
    private MetodoPago elegido;

    private FacturarDialog(Window padre, CobroFila cobro) {
        super(padre, "Facturar ticket N° " + cobro.nroTicket(), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        cmbMetodo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof MetodoPago m) {
                    setText(CobrosPanel.texto(m));
                }
                return this;
            }
        });
        cmbMetodo.setSelectedItem(cobro.metodoElegidoPorCliente() != null
                ? cobro.metodoElegidoPorCliente() : MetodoPago.EFECTIVO);

        JLabel titulo = new JLabel("Ticket N° " + cobro.nroTicket() + " · " + cobro.cliente());
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 15f));

        JPanel datos = new JPanel(new GridLayout(0, 2, 12, 6));
        datos.add(new JLabel("Consumo"));
        datos.add(new JLabel(Formatos.pesos(cobro.consumo()) + " (" + cobro.cantidadPedidos() + (cobro.cantidadPedidos() == 1 ? " pedido)" : " pedidos)")));
        datos.add(new JLabel("Método de pago"));
        datos.add(cmbMetodo);
        if (cobro.metodoElegidoPorCliente() != null) {
            datos.add(new JLabel(" "));
            datos.add(new JLabel("El cliente eligió " + CobrosPanel.texto(cobro.metodoElegidoPorCliente())));
        }

        JButton btnFacturar = new JButton("Facturar");
        JButton btnCancelar = new JButton("Cancelar");
        btnFacturar.addActionListener(e -> {
            elegido = (MetodoPago) cmbMetodo.getSelectedItem();
            dispose();
        });
        btnCancelar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnCancelar);
        botones.add(btnFacturar);
        getRootPane().setDefaultButton(btnFacturar);

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        raiz.add(titulo, BorderLayout.NORTH);
        raiz.add(datos, BorderLayout.CENTER);
        raiz.add(botones, BorderLayout.SOUTH);
        setContentPane(raiz);

        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    /** Abre la ventana y devuelve el método elegido, o vacío si se canceló. */
    public static Optional<MetodoPago> pedirMetodo(Component padre, CobroFila cobro) {
        Window ventana = padre instanceof Window w ? w : SwingUtilities.getWindowAncestor(padre);
        FacturarDialog dialogo = new FacturarDialog(ventana, cobro);
        dialogo.setVisible(true);
        return Optional.ofNullable(dialogo.elegido);
    }
}