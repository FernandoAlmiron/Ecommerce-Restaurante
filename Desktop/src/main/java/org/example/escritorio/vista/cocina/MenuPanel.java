package org.example.escritorio.vista.cocina;

import org.example.Modelo.menu.Menu;
import org.example.escritorio.controlador.cocina.MenuControlador;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Formatos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

/**
 * La carta del restaurante (RF-26 a RF-28): alta y modificación de platos y su receta.
 * Eliminar un plato es solo para ADMIN (RF-27): el botón no aparece para los demás.
 */
public class MenuPanel extends JPanel {

    private final MenuTableModel modelo = new MenuTableModel();
    private final JTable tabla = new JTable(modelo);

    private final JButton btnActualizar = new JButton("Actualizar");
    private final JButton btnNuevo = new JButton("Nuevo plato");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnReceta = new JButton("Receta");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JLabel lblResumen = new JLabel(" ");

    private final MenuControlador controlador;

    public MenuPanel() {
        super(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(armarEncabezado(), BorderLayout.NORTH);
        add(armarTabla(), BorderLayout.CENTER);
        add(armarAcciones(), BorderLayout.SOUTH);

        controlador = new MenuControlador(this);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                habilitarAcciones(platoSeleccionado().isPresent());
            }
        });
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.getSelectedRow() >= 0) {
                    controlador.modificar(); // doble clic = Modificar
                }
            }
        });
        btnActualizar.addActionListener(e -> controlador.cargar());
        btnNuevo.addActionListener(e -> controlador.nuevo());
        btnModificar.addActionListener(e -> controlador.modificar());
        btnReceta.addActionListener(e -> controlador.receta());
        btnEliminar.addActionListener(e -> controlador.eliminar());

        habilitarAcciones(false);
        controlador.cargar();
    }

    private JPanel armarEncabezado() {
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izquierda.add(btnActualizar);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(btnNuevo);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.add(izquierda, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);
        return encabezado;
    }

    private JScrollPane armarTabla() {
        TableRowSorter<MenuTableModel> orden = new TableRowSorter<>(modelo);
        orden.setSortKeys(List.of(new RowSorter.SortKey(MenuTableModel.COL_NOMBRE, SortOrder.ASCENDING)));
        tabla.setRowSorter(orden);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(26);
        Formatos.aplicarA(tabla);
        int[] anchos = {240, 320, 110, 80};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        return new JScrollPane(tabla);
    }

    private JPanel armarAcciones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        if (Sesion.esAdmin()) {
            botones.add(btnEliminar);
        }
        botones.add(btnReceta);
        botones.add(btnModificar);

        JPanel acciones = new JPanel(new BorderLayout());
        acciones.add(lblResumen, BorderLayout.WEST);
        acciones.add(botones, BorderLayout.EAST);
        return acciones;
    }

    // ---------- lo que usa el controlador ----------

    public void mostrarPlatos(List<Menu> platos) {
        modelo.setFilas(platos);
        long disponibles = platos.stream().filter(Menu::isDisponible).count();
        lblResumen.setText(platos.size() + " platos · " + disponibles + " disponibles");
        habilitarAcciones(platoSeleccionado().isPresent());
    }

    public Optional<Menu> platoSeleccionado() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return Optional.empty();
        }
        return Optional.of(modelo.getFila(tabla.convertRowIndexToModel(filaVista)));
    }

    public void habilitarAcciones(boolean hayPlato) {
        btnModificar.setEnabled(hayPlato);
        btnReceta.setEnabled(hayPlato);
        btnEliminar.setEnabled(hayPlato);
    }

    public JButton getBotonActualizar() { return btnActualizar; }
    public JButton getBotonEliminar() { return btnEliminar; }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public boolean confirmar(String pregunta) {
        return Dialogos.confirmar(this, pregunta);
    }
}