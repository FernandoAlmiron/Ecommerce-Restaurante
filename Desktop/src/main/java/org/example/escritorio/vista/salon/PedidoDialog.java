package org.example.escritorio.vista.salon;

import org.example.Modelo.menu.Menu;
import org.example.escritorio.controlador.salon.PedidoControlador;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Formatos;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.List;

/**
 * Ventana para cargar pedidos a una mesa (RF-20). Queda abierta para cargar varios platos seguidos;
 * cada "Agregar" se guarda en el momento (el backend descuenta stock en ese momento).
 */
public class PedidoDialog extends JDialog {

    private final int nroTicket;

    private final JComboBox<Menu> cmbMenu = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
    private final JTextField txtObservaciones = new JTextField(24);
    private final JLabel lblUltimo = new JLabel(" ");
    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnListo = new JButton("Listo");

    private final PedidoControlador controlador;
    private boolean huboCambios;

    public PedidoDialog(Window padre, int nroTicket) {
        super(padre, "Agregar pedido — Ticket N° " + nroTicket, ModalityType.APPLICATION_MODAL);
        this.nroTicket = nroTicket;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        raiz.add(armarFormulario(), BorderLayout.CENTER);
        raiz.add(armarBotones(), BorderLayout.SOUTH);
        setContentPane(raiz);

        cmbMenu.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof Menu m) {
                    setText(m.getNombre() + " — " + Formatos.pesos(m.getPrecio()));
                }
                return this;
            }
        });

        controlador = new PedidoControlador(this);

        btnAgregar.addActionListener(e -> controlador.agregar());
        txtObservaciones.addActionListener(e -> controlador.agregar()); // Enter en observaciones = Agregar
        btnListo.addActionListener(e -> dispose());

        controlador.cargarMenus();

        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    private JPanel armarFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 12);

        JLabel titulo = new JLabel("Pedido para el ticket N° " + nroTicket);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(titulo, c);
        c.gridwidth = 1;

        fila(panel, c, 1, "Plato", cmbMenu);
        fila(panel, c, 2, "Cantidad", spnCantidad);
        fila(panel, c, 3, "Observaciones", txtObservaciones);

        lblUltimo.setForeground(new Color(0x2E7D32));
        c.gridx = 1; c.gridy = 4;
        panel.add(lblUltimo, c);
        return panel;
    }

    private void fila(JPanel panel, GridBagConstraints c, int y, String etiqueta, JComponent campo) {
        c.gridx = 0; c.gridy = y; c.weightx = 0;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1; c.weightx = 1;
        panel.add(campo, c);
    }

    private JPanel armarBotones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnListo);
        botones.add(btnAgregar);
        getRootPane().setDefaultButton(btnAgregar);
        return botones;
    }

    // ---------- lo que usa el controlador ----------

    public int getNroTicket() { return nroTicket; }
    public Menu getMenuSeleccionado() { return (Menu) cmbMenu.getSelectedItem(); }
    public int getCantidad() { return (Integer) spnCantidad.getValue(); }
    public String getObservaciones() { return txtObservaciones.getText().trim(); }

    public JButton getBotonAgregar() { return btnAgregar; }
    public JComboBox<Menu> getComboMenu() { return cmbMenu; }

    public void mostrarMenus(List<Menu> menus) {
        cmbMenu.removeAllItems();
        menus.forEach(cmbMenu::addItem);
        btnAgregar.setEnabled(!menus.isEmpty());
        if (menus.isEmpty()) {
            lblUltimo.setForeground(Color.GRAY);
            lblUltimo.setText("No hay platos disponibles en el menú.");
        }
    }

    /** El pedido se guardó: se avisa abajo y se deja listo para cargar el siguiente. */
    public void pedidoAgregado(String descripcion) {
        huboCambios = true;
        lblUltimo.setText("Agregado: " + descripcion);
        spnCantidad.setValue(1);
        txtObservaciones.setText("");
        cmbMenu.requestFocusInWindow();
    }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    /** true si se guardó al menos un pedido: la pantalla de Salón tiene que recargar. */
    public boolean huboCambios() {
        return huboCambios;
    }
}