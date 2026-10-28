package org.example.escritorio.vista.salon;

import org.example.Modelo.menu.Menu;
import org.example.escritorio.controlador.salon.PedidoControlador;
import org.example.escritorio.controlador.salon.PedidoControlador.Renglon;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.DialogoFormulario;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formulario;
import org.example.escritorio.vista.comun.Icono;
import org.example.escritorio.vista.comun.Tablas;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.List;

/**
 * Ventana para cargar pedidos a una mesa (RF-20). Los platos se van juntando en una lista
 * (se puede cambiar la cantidad o sacar alguno) y se guardan todos al tocar "Cargar pedido";
 * recién ahí el backend descuenta stock y le llegan a la cocina.
 */
public class PedidoDialog extends DialogoFormulario {

    private final int nroTicket;

    private final JComboBox<Menu> cmbMenu = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
    private final JTextField txtObservaciones = new JTextField(24);
    private final JButton btnAgregar = new Boton("Agregar a la lista", new Icono(Icono.Tipo.MAS, 16, Estilo.CAFE),
            Boton.Tipo.CONTORNO_CAFE);

    private final JLabel lblLista = new JLabel();
    private final JLabel lblTotal = new JLabel();
    private final JPanel filas = new JPanel();

    private final PedidoControlador controlador;
    private boolean huboCambios;
    /** true mientras se guarda la lista: no se puede tocar nada ni cerrar la ventana. */
    private boolean guardando;

    public PedidoDialog(Window padre, int nroTicket) {
        super(padre, "Salón · Ticket #T-" + nroTicket, "Agregar pedido",
                "Armá la lista y cargala toda junta: recién ahí le llega a la cocina.",
                "Cargar pedido");
        this.nroTicket = nroTicket;

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

        btnAgregar.addActionListener(e -> controlador.agregar());
        txtObservaciones.addActionListener(e -> controlador.agregar()); // Enter en observaciones = Agregar
        btnPrincipal.addActionListener(e -> controlador.cargar());

        controlador.cargarMenus();

        armar();
        getRootPane().setDefaultButton(btnAgregar); // Enter suma a la lista; cargar es a propósito
    }

    @Override
    protected int anchoMinimo() {
        return 660;
    }

    @Override
    protected JComponent armarCuerpo() {
        Formulario.estilizar(cmbMenu);
        // el ancho lo da la ventana: si no, un plato de nombre largo agranda el combo y aplasta la lista
        cmbMenu.setPreferredSize(new Dimension(100, cmbMenu.getPreferredSize().height));
        Formulario.estilizar(spnCantidad);
        Formulario.estilizar(txtObservaciones, "Ej.: sin queso, poco picante");
        btnAgregar.setBorder(Estilo.relleno(11, 18, 11, 20));

        // cantidad angosta y observaciones con el resto del ancho
        JComponent cantidad = Formulario.campo("Cantidad", spnCantidad, null);
        cantidad.setPreferredSize(new Dimension(130, cantidad.getPreferredSize().height));
        JPanel cantidadYObs = Estilo.transparente(new BorderLayout(16, 0));
        cantidadYObs.add(cantidad, BorderLayout.WEST);
        cantidadYObs.add(Formulario.campo("Observaciones", txtObservaciones, null), BorderLayout.CENTER);

        JPanel agregar = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        agregar.add(btnAgregar);

        JComponent lista = armarLista();
        mostrarRenglones(List.of()); // con los textos puestos, así la ventana se mide con su alto real
        return Formulario.cuerpo(
                Formulario.campo("Plato", cmbMenu, null),
                cantidadYObs,
                agregar,
                lista);
    }

    /** "PARA CARGAR · 2 platos" con el total y, abajo, los platos de la lista. */
    private JComponent armarLista() {
        lblLista.setFont(Estilo.conEspaciado(Estilo.fuente(Font.BOLD, 13f), 0.1f));
        lblLista.setForeground(Estilo.CAFE);
        lblLista.setBorder(Estilo.relleno(0, 0, 0, 8));
        lblTotal.setFont(Estilo.fuente(Font.BOLD, 16f));
        lblTotal.setForeground(Estilo.TEXTO);
        JPanel encabezado = Estilo.transparente(new BorderLayout());
        encabezado.add(lblLista, BorderLayout.WEST);
        encabezado.add(lblTotal, BorderLayout.EAST);

        filas.setLayout(new BoxLayout(filas, BoxLayout.Y_AXIS));
        filas.setOpaque(false);
        JPanel arriba = Estilo.transparente(new BorderLayout());
        arriba.add(filas, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(arriba);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(100, 160));
        scroll.setMinimumSize(scroll.getPreferredSize());

        Estilo.Tarjeta caja = new Estilo.Tarjeta(new BorderLayout(), 16);
        caja.setBackground(Estilo.ENCABEZADO_TABLA);
        caja.add(scroll, BorderLayout.CENTER);

        JPanel lista = Estilo.transparente(new BorderLayout(0, 10));
        lista.add(encabezado, BorderLayout.NORTH);
        lista.add(caja, BorderLayout.CENTER);
        return lista;
    }

    /** Un plato de la lista: cantidad y nombre, observaciones, subtotal, − / + y sacar. */
    private JComponent crearFila(Renglon r, int indice, boolean ultima) {
        JLabel nombre = new JLabel(r.cantidad() + " × " + r.menu().getNombre());
        nombre.setFont(Estilo.fuente(Font.BOLD, 16f));
        nombre.setForeground(Estilo.TEXTO);
        JPanel textos = Estilo.transparente(null);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(nombre);
        if (r.observaciones() != null) {
            JLabel obs = new JLabel(r.observaciones());
            obs.setFont(Estilo.fuente(Font.ITALIC, 14f));
            obs.setForeground(Estilo.TEXTO_SUAVE);
            textos.add(Box.createVerticalStrut(2));
            textos.add(obs);
        }

        JLabel subtotal = new JLabel(Tablas.pesosCortos(r.subtotal()));
        subtotal.setFont(Estilo.fuente(Font.BOLD, 15f));
        subtotal.setForeground(Estilo.TEXTO);
        subtotal.setBorder(Estilo.relleno(0, 0, 0, 14));
        JButton menos = chico(Icono.Tipo.MENOS, Estilo.TEXTO, "Uno menos");
        menos.addActionListener(e -> controlador.cambiarCantidad(indice, -1));
        JButton mas = chico(Icono.Tipo.MAS, Estilo.TEXTO, "Uno más");
        mas.addActionListener(e -> controlador.cambiarCantidad(indice, 1));
        JButton sacar = chico(Icono.Tipo.TACHO, Estilo.ROJO_TEXTO, "Sacar de la lista");
        sacar.addActionListener(e -> controlador.quitar(indice));

        JPanel acciones = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        acciones.add(subtotal);
        acciones.add(menos);
        acciones.add(mas);
        acciones.add(Box.createHorizontalStrut(4));
        acciones.add(sacar);
        JPanel accionesCentradas = Estilo.transparente(new java.awt.GridBagLayout());
        accionesCentradas.add(acciones);

        JPanel fila = Estilo.transparente(new BorderLayout(12, 0));
        fila.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, ultima ? 0 : 1, 0, Estilo.BORDE),
                Estilo.relleno(10, 16, 10, 12)));
        fila.add(textos, BorderLayout.CENTER);
        fila.add(accionesCentradas, BorderLayout.EAST);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));
        return fila;
    }

    private static JButton chico(Icono.Tipo icono, java.awt.Color color, String ayuda) {
        JButton b = Boton.soloIcono(icono, color, ayuda);
        b.setBorder(Estilo.relleno(7, 7, 7, 7));
        return b;
    }

    // ---------- lo que usa el controlador ----------

    public int getNroTicket() { return nroTicket; }
    public Menu getMenuSeleccionado() { return (Menu) cmbMenu.getSelectedItem(); }
    public int getCantidad() { return (Integer) spnCantidad.getValue(); }
    public String getObservaciones() { return txtObservaciones.getText().trim(); }

    public JButton getBotonCargar() { return btnPrincipal; }
    public JComboBox<Menu> getComboMenu() { return cmbMenu; }

    public void mostrarMenus(List<Menu> menus) {
        cmbMenu.removeAllItems();
        menus.forEach(cmbMenu::addItem);
        btnAgregar.setEnabled(!menus.isEmpty());
        if (menus.isEmpty()) {
            mostrarError("No hay platos disponibles en el menú.");
        }
    }

    /** Redibuja la lista de platos para cargar, con su total. */
    public void mostrarRenglones(List<Renglon> renglones) {
        filas.removeAll();
        if (renglones.isEmpty()) {
            JLabel vacio = new JLabel("Todavía no agregaste nada. Elegí un plato y tocá \"Agregar a la lista\".");
            vacio.setFont(Estilo.fuente(Font.PLAIN, 14f));
            vacio.setForeground(Estilo.TEXTO_SUAVE);
            vacio.setBorder(Estilo.relleno(18, 16, 18, 16));
            vacio.setAlignmentX(Component.LEFT_ALIGNMENT);
            filas.add(vacio);
        }
        for (int i = 0; i < renglones.size(); i++) {
            filas.add(crearFila(renglones.get(i), i, i == renglones.size() - 1));
        }
        int platos = renglones.stream().mapToInt(Renglon::cantidad).sum();
        BigDecimal total = renglones.stream().map(Renglon::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        lblLista.setText("PARA CARGAR" + (platos == 0 ? "" : " · " + platos + (platos == 1 ? " PLATO" : " PLATOS")));
        lblTotal.setText(platos == 0 ? "" : "Total " + Tablas.pesosCortos(total));
        btnPrincipal.setEnabled(!renglones.isEmpty());
        btnPrincipal.setText(renglones.isEmpty() ? "Cargar pedido" : "Cargar pedido (" + platos + ")");
        filas.revalidate();
        filas.repaint();
    }

    /** Se sumó a la lista: se deja listo para elegir el siguiente plato. */
    public void listoParaOtro() {
        spnCantidad.setValue(1);
        txtObservaciones.setText("");
        cmbMenu.requestFocusInWindow();
    }

    /**
     * Mientras se guarda la lista no se puede agregar, cambiar ni sacar nada (si no, lo que se toca
     * en ese momento se perdería o se sacaría el plato equivocado). El botón Cargar lo maneja Tarea.
     */
    public void bloquear(boolean bloqueado) {
        guardando = bloqueado;
        for (JComponent c : new JComponent[]{cmbMenu, spnCantidad, txtObservaciones, btnAgregar, btnCancelar}) {
            c.setEnabled(!bloqueado);
        }
        habilitarTodo(filas, !bloqueado);
    }

    private static void habilitarTodo(java.awt.Container contenedor, boolean habilitado) {
        for (Component c : contenedor.getComponents()) {
            c.setEnabled(habilitado);
            if (c instanceof java.awt.Container hijo) {
                habilitarTodo(hijo, habilitado);
            }
        }
    }

    /** Algunos platos ya quedaron guardados (la pantalla de Salón tiene que recargar). */
    public void marcarCambios() {
        huboCambios = true;
    }

    /** Se cargó toda la lista: se cierra la ventana. */
    public void cerrarCargado() {
        huboCambios = true;
        dispose();
    }

    /** Si quedan platos sin cargar, se pregunta antes de cerrar (Cancelar o la X); mientras guarda, no se cierra. */
    @Override
    public void dispose() {
        if (guardando) {
            return;
        }
        if (isShowing() && controlador.hayPendientes()
                && !Dialogos.confirmar(this, "Hay platos en la lista que todavía no se cargaron. ¿Descartarlos?")) {
            return;
        }
        super.dispose();
    }

    /** true si se guardó al menos un pedido: la pantalla de Salón tiene que recargar. */
    public boolean huboCambios() {
        return huboCambios;
    }
}
