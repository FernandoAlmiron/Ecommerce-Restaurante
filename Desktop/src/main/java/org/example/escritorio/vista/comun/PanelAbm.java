package org.example.escritorio.vista.comun;

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
import java.util.function.Predicate;

/**
 * Pantalla base para un ABM (alta, baja y modificación): una tabla con Actualizar, Nuevo, Modificar y Eliminar.
 * Cada pantalla concreta (zonas, sectores, proveedores...) la extiende, le pasa su modelo de tabla
 * y conecta los botones con su controlador. Puede sumar botones propios con agregarBoton.
 */
public abstract class PanelAbm<T> extends JPanel {

    private final ModeloTablaBase<T> modelo;
    private final JTable tabla;

    private final JButton btnActualizar = new JButton("Actualizar");
    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JPanel botonesExtra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private final JLabel lblResumen = new JLabel(" ");

    /**
     * @param modelo        columnas de la tabla
     * @param columnaOrden  columna por la que se ordena al abrir (ascendente)
     * @param anchos        ancho preferido de cada columna
     * @param textoNuevo    texto del botón de alta, por ejemplo "Nueva zona"
     */
    protected PanelAbm(ModeloTablaBase<T> modelo, int columnaOrden, int[] anchos, String textoNuevo) {
        super(new BorderLayout(0, 8));
        this.modelo = modelo;
        this.tabla = new JTable(modelo);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        btnNuevo.setText(textoNuevo);

        TableRowSorter<ModeloTablaBase<T>> orden = new TableRowSorter<>(modelo);
        orden.setSortKeys(List.of(new RowSorter.SortKey(columnaOrden, SortOrder.ASCENDING)));
        tabla.setRowSorter(orden);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(26);
        Formatos.aplicarA(tabla);
        for (int i = 0; i < anchos.length && i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izquierda.add(btnActualizar);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(btnNuevo);
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.add(izquierda, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(botonesExtra);
        botones.add(btnEliminar);
        botones.add(btnModificar);
        JPanel pie = new JPanel(new BorderLayout());
        pie.add(lblResumen, BorderLayout.WEST);
        pie.add(botones, BorderLayout.EAST);

        add(encabezado, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);

        habilitarAcciones(false);
    }

    /** Conecta los botones y la selección con el controlador. Llamarlo al final del constructor de la subclase. */
    protected void conectar(Runnable cargar, Runnable nuevo, Runnable modificar, Runnable eliminar,
                            Runnable alCambiarSeleccion) {
        btnActualizar.addActionListener(e -> cargar.run());
        btnNuevo.addActionListener(e -> nuevo.run());
        btnModificar.addActionListener(e -> modificar.run());
        btnEliminar.addActionListener(e -> eliminar.run());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                alCambiarSeleccion.run();
            }
        });
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && btnModificar.isEnabled()) {
                    modificar.run(); // doble clic = Modificar
                }
            }
        });
    }

    /** Suma un botón propio de la pantalla (por ejemplo, Activar), a la izquierda de Eliminar. */
    protected void agregarBoton(JButton boton) {
        botonesExtra.add(boton);
    }

    // ---------- lo que usa el controlador ----------

    public void mostrar(List<T> filas, String resumen) {
        modelo.setFilas(filas);
        lblResumen.setText(resumen);
    }

    public Optional<T> seleccionado() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return Optional.empty();
        }
        return Optional.of(modelo.getFila(tabla.convertRowIndexToModel(filaVista)));
    }

    /** Deja seleccionada la primera fila que cumpla la condición (por ejemplo, la que se acaba de guardar). */
    public void seleccionar(Predicate<T> cual) {
        for (int fila = 0; fila < tabla.getRowCount(); fila++) {
            if (cual.test(modelo.getFila(tabla.convertRowIndexToModel(fila)))) {
                tabla.setRowSelectionInterval(fila, fila);
                tabla.scrollRectToVisible(tabla.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    public void habilitarAcciones(boolean hayFila) {
        btnModificar.setEnabled(hayFila);
        btnEliminar.setEnabled(hayFila);
    }

    public JButton getBotonActualizar() { return btnActualizar; }
    public JButton getBotonNuevo() { return btnNuevo; }
    public JButton getBotonModificar() { return btnModificar; }
    public JButton getBotonEliminar() { return btnEliminar; }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public boolean confirmar(String pregunta) {
        return Dialogos.confirmar(this, pregunta);
    }
}