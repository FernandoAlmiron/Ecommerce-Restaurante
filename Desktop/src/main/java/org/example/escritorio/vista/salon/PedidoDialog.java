package org.example.escritorio.vista.salon;

import org.example.Modelo.menu.Menu;
import org.example.escritorio.controlador.salon.PedidoControlador;
import org.example.escritorio.vista.comun.DialogoFormulario;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formulario;
import org.example.escritorio.vista.comun.Tablas;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.Component;
import java.awt.Window;
import java.util.List;

/**
 * Ventana para cargar pedidos a una mesa (RF-20). Queda abierta para cargar varios platos seguidos;
 * cada "Agregar" se guarda en el momento (el backend descuenta stock en ese momento).
 */
public class PedidoDialog extends DialogoFormulario {

    private final int nroTicket;

    private final JComboBox<Menu> cmbMenu = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
    private final JTextField txtObservaciones = new JTextField(24);
    private final JLabel lblUltimo = Formulario.nota(new JLabel(" "));

    private final PedidoControlador controlador;
    private boolean huboCambios;

    public PedidoDialog(Window padre, int nroTicket) {
        super(padre, "Salón · Ticket #T-" + nroTicket, "Agregar pedido",
                "Cada plato se guarda al tocar Agregar y podés seguir cargando.",
                "Agregar");
        this.nroTicket = nroTicket;
        btnCancelar.setText("Listo");

        cmbMenu.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof Menu m) {
                    setText(m.getNombre() + " — " + Tablas.pesosCortos(m.getPrecio()));
                }
                return this;
            }
        });

        controlador = new PedidoControlador(this);

        btnPrincipal.addActionListener(e -> controlador.agregar());
        txtObservaciones.addActionListener(e -> controlador.agregar()); // Enter en observaciones = Agregar

        controlador.cargarMenus();

        armar();
    }

    @Override
    protected JComponent armarCuerpo() {
        Formulario.estilizar(cmbMenu);
        Formulario.estilizar(spnCantidad);
        Formulario.estilizar(txtObservaciones, "Ej.: sin queso, poco picante");
        return Formulario.cuerpo(
                Formulario.campo("Plato", cmbMenu, null),
                Formulario.campo("Cantidad", spnCantidad, null),
                Formulario.campo("Observaciones", txtObservaciones, "Opcional. Le llegan a la cocina con la comanda."),
                lblUltimo);
    }

    // ---------- lo que usa el controlador ----------

    public int getNroTicket() { return nroTicket; }
    public Menu getMenuSeleccionado() { return (Menu) cmbMenu.getSelectedItem(); }
    public int getCantidad() { return (Integer) spnCantidad.getValue(); }
    public String getObservaciones() { return txtObservaciones.getText().trim(); }

    public JButton getBotonAgregar() { return btnPrincipal; }
    public JComboBox<Menu> getComboMenu() { return cmbMenu; }

    public void mostrarMenus(List<Menu> menus) {
        cmbMenu.removeAllItems();
        menus.forEach(cmbMenu::addItem);
        btnPrincipal.setEnabled(!menus.isEmpty());
        if (menus.isEmpty()) {
            lblUltimo.setForeground(Estilo.TEXTO_SUAVE);
            lblUltimo.setText("No hay platos disponibles en el menú.");
        }
    }

    /** El pedido se guardó: se avisa abajo y se deja listo para cargar el siguiente. */
    public void pedidoAgregado(String descripcion) {
        huboCambios = true;
        lblUltimo.setForeground(Estilo.VERDE_TEXTO);
        lblUltimo.setText("✓ Agregado: " + descripcion);
        spnCantidad.setValue(1);
        txtObservaciones.setText("");
        cmbMenu.requestFocusInWindow();
    }

    /** true si se guardó al menos un pedido: la pantalla de Salón tiene que recargar. */
    public boolean huboCambios() {
        return huboCambios;
    }
}
