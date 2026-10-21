package org.example.escritorio.vista.rrhh;

import org.example.Modelo.persona.Empleado;
import org.example.escritorio.controlador.rrhh.EmpleadosControlador;
import org.example.escritorio.vista.comun.Dialogos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

/**
 * Recursos humanos (RF-53 a RF-55): alta, modificación, cambio de sector y baja de empleados.
 * Los empleados de ADMINISTRACION solo los puede tocar un ADMIN: para RRHH esos botones quedan deshabilitados.
 */
public class EmpleadosPanel extends JPanel {

    private static final String TODOS = "Todos";

    private final EmpleadoTableModel modelo = new EmpleadoTableModel();
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<EmpleadoTableModel> orden = new TableRowSorter<>(modelo);

    private final JTextField txtBuscar = new JTextField(16);
    private final JComboBox<String> cmbSector = new JComboBox<>(new String[]{TODOS});
    private final JButton btnActualizar = new JButton("Actualizar");
    private final JButton btnNuevo = new JButton("Nuevo empleado");
    private final JButton btnModificar = new JButton("Modificar datos");
    private final JButton btnCambiarSector = new JButton("Cambiar sector");
    private final JButton btnBaja = new JButton("Dar de baja");
    private final JLabel lblResumen = new JLabel(" ");

    private final EmpleadosControlador controlador;

    public EmpleadosPanel() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(armarEncabezado(), BorderLayout.NORTH);
        add(armarTabla(), BorderLayout.CENTER);
        add(armarAcciones(), BorderLayout.SOUTH);

        controlador = new EmpleadosControlador(this);

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { aplicarFiltro(); }
            @Override public void removeUpdate(DocumentEvent e) { aplicarFiltro(); }
            @Override public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
        });
        cmbSector.addActionListener(e -> aplicarFiltro());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                controlador.alCambiarSeleccion();
            }
        });
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && btnModificar.isEnabled()) {
                    controlador.modificar(); // doble clic = Modificar datos
                }
            }
        });
        btnActualizar.addActionListener(e -> controlador.cargar(null));
        btnNuevo.addActionListener(e -> controlador.nuevo());
        btnModificar.addActionListener(e -> controlador.modificar());
        btnCambiarSector.addActionListener(e -> controlador.cambiarSector());
        btnBaja.addActionListener(e -> controlador.darDeBaja());

        habilitarAcciones(false);
        controlador.cargar(null);
    }

    // ---------- armado ----------

    private JPanel armarEncabezado() {
        JLabel titulo = new JLabel("Recursos humanos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.add(new JLabel("Buscar:"));
        filtros.add(txtBuscar);
        filtros.add(new JLabel("Sector:"));
        filtros.add(cmbSector);
        filtros.add(btnActualizar);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(btnNuevo);

        JPanel fila = new JPanel(new BorderLayout());
        fila.add(filtros, BorderLayout.CENTER);
        fila.add(derecha, BorderLayout.EAST);

        JPanel encabezado = new JPanel(new BorderLayout(0, 12));
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(fila, BorderLayout.CENTER);
        return encabezado;
    }

    private JScrollPane armarTabla() {
        orden.setSortKeys(List.of(new RowSorter.SortKey(EmpleadoTableModel.COL_APELLIDO, SortOrder.ASCENDING)));
        tabla.setRowSorter(orden);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(26);
        int[] anchos = {60, 220, 100, 120, 130, 150};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        return new JScrollPane(tabla);
    }

    private JPanel armarAcciones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnBaja);
        botones.add(btnCambiarSector);
        botones.add(btnModificar);

        JPanel acciones = new JPanel(new BorderLayout());
        acciones.add(lblResumen, BorderLayout.WEST);
        acciones.add(botones, BorderLayout.EAST);
        return acciones;
    }

    // ---------- lo que usa el controlador ----------

    public void mostrarEmpleados(List<Empleado> empleados) {
        String elegido = (String) cmbSector.getSelectedItem();
        modelo.setFilas(empleados);
        // el combo de filtro se arma con los sectores que tienen empleados
        cmbSector.removeAllItems();
        cmbSector.addItem(TODOS);
        empleados.stream().map(EmpleadoTableModel::sector).filter(s -> !s.isEmpty())
                .distinct().sorted().forEach(cmbSector::addItem);
        cmbSector.setSelectedItem(elegido);
        aplicarFiltro();
    }

    public Optional<Empleado> empleadoSeleccionado() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return Optional.empty();
        }
        return Optional.of(modelo.getFila(tabla.convertRowIndexToModel(filaVista)));
    }

    public void seleccionarEmpleado(int legajo) {
        for (int fila = 0; fila < tabla.getRowCount(); fila++) {
            if (modelo.getFila(tabla.convertRowIndexToModel(fila)).getLegajo() == legajo) {
                tabla.setRowSelectionInterval(fila, fila);
                tabla.scrollRectToVisible(tabla.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    public void habilitarAcciones(boolean habilitado) {
        btnModificar.setEnabled(habilitado);
        btnCambiarSector.setEnabled(habilitado);
        btnBaja.setEnabled(habilitado);
    }

    public JButton getBotonActualizar() { return btnActualizar; }
    public JButton getBotonCambiarSector() { return btnCambiarSector; }
    public JButton getBotonBaja() { return btnBaja; }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public void mostrarInfo(String mensaje) {
        Dialogos.info(this, mensaje);
    }

    public boolean confirmar(String pregunta) {
        return Dialogos.confirmar(this, pregunta);
    }

    // ---------- filtro ----------

    private void aplicarFiltro() {
        String texto = txtBuscar.getText().trim().toLowerCase();
        String sector = (String) cmbSector.getSelectedItem();
        orden.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends EmpleadoTableModel, ? extends Integer> fila) {
                Empleado e = fila.getModel().getFila(fila.getIdentifier());
                boolean coincideSector = sector == null || TODOS.equals(sector)
                        || sector.equals(EmpleadoTableModel.sector(e));
                boolean coincideTexto = texto.isEmpty()
                        || (e.getApellido() + " " + e.getNombre()).toLowerCase().contains(texto)
                        || (e.getUsername() != null && e.getUsername().toLowerCase().contains(texto))
                        || String.valueOf(e.getDni()).contains(texto);
                return coincideSector && coincideTexto;
            }
        });
        lblResumen.setText(tabla.getRowCount() == modelo.getRowCount()
                ? modelo.getRowCount() + " empleados"
                : "Mostrando " + tabla.getRowCount() + " de " + modelo.getRowCount() + " empleados");
    }
}