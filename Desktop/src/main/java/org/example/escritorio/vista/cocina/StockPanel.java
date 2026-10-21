package org.example.escritorio.vista.cocina;

import org.example.Modelo.inventario.Stock;
import org.example.escritorio.controlador.cocina.StockControlador;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Icono;
import org.example.escritorio.vista.comun.Tablas;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stock de insumos (RF-30, RF-31): alta, modificación, reponer y ajustar.
 * Los insumos bajo mínimo se marcan en rojo; eliminar es solo para ADMIN (RF-32).
 */
public class StockPanel extends JPanel {

    /** Propiedad que avisa cuántos insumos están bajo mínimo (la usa la solapa). */
    public static final String PROP_BAJO_MINIMO = "insumosBajoMinimo";

    private static final int ALTO_FILA = 58;

    private final StockTableModel modelo = new StockTableModel();
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<StockTableModel> orden = new TableRowSorter<>(modelo);

    private final JCheckBox chkSoloBajoMinimo = new JCheckBox("Solo bajo mínimo");
    private final JButton btnActualizar = new Boton(null, new Icono(Icono.Tipo.ACTUALIZAR, 20, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);
    private final JButton btnNuevo = new Boton("Nuevo insumo", new Icono(Icono.Tipo.MAS, 16, Color.WHITE),
            Boton.Tipo.PRIMARIO);
    private final JButton btnModificar = new Boton("Modificar", Boton.Tipo.CONTORNO);
    private final JButton btnReponer = new Boton("Reponer", Boton.Tipo.OSCURO);
    private final JButton btnAjustar = new Boton("Ajustar", Boton.Tipo.CONTORNO);
    private final JButton btnEliminar = new Boton("Eliminar", Boton.Tipo.CONTORNO_CAFE);
    private final JLabel lblResumen = new JLabel(" ");
    private final JLabel lblResumenDetalle = new JLabel(" ");

    private final StockControlador controlador;

    public StockPanel() {
        super(new BorderLayout(0, 22));
        setOpaque(false);
        setBorder(Estilo.relleno(4, 0, 0, 0));

        add(armarEncabezado(), BorderLayout.NORTH);
        add(armarTabla(), BorderLayout.CENTER);
        add(armarAcciones(), BorderLayout.SOUTH);

        controlador = new StockControlador(this);

        chkSoloBajoMinimo.addActionListener(e -> aplicarFiltro());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                habilitarAcciones(insumoSeleccionado().isPresent());
            }
        });
        btnActualizar.addActionListener(e -> controlador.cargar());
        btnNuevo.addActionListener(e -> controlador.nuevo());
        btnModificar.addActionListener(e -> controlador.modificar());
        btnReponer.addActionListener(e -> controlador.reponer());
        btnAjustar.addActionListener(e -> controlador.ajustar());
        btnEliminar.addActionListener(e -> controlador.eliminar());

        habilitarAcciones(false);
        controlador.cargar();
    }

    private JComponent armarEncabezado() {
        lblResumen.setFont(Estilo.fuente(Font.BOLD, 16f));
        lblResumen.setForeground(Estilo.TEXTO);
        lblResumenDetalle.setFont(Estilo.fuente(Font.PLAIN, 16f));
        lblResumenDetalle.setForeground(Estilo.TEXTO_SUAVE);
        JPanel resumen = Estilo.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        resumen.add(lblResumen);
        resumen.add(lblResumenDetalle);
        JPanel resumenCentrado = Estilo.transparente(new GridBagLayout());
        resumenCentrado.add(resumen);

        chkSoloBajoMinimo.setFont(Estilo.fuente(Font.PLAIN, 16f));
        chkSoloBajoMinimo.setForeground(Estilo.TEXTO);
        chkSoloBajoMinimo.setOpaque(false);
        chkSoloBajoMinimo.setIconTextGap(10);
        chkSoloBajoMinimo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Estilo.casilla(chkSoloBajoMinimo);
        Estilo.Tarjeta cajaFiltro = new Estilo.Tarjeta(new GridBagLayout(), 14);
        cajaFiltro.setBorder(Estilo.relleno(9, 16, 9, 18));
        cajaFiltro.add(chkSoloBajoMinimo);

        btnActualizar.setToolTipText("Actualizar");
        btnActualizar.setBorder(Estilo.relleno(12, 12, 12, 12));

        JPanel derecha = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(cajaFiltro);
        derecha.add(Box.createHorizontalStrut(14));
        derecha.add(btnActualizar);
        derecha.add(Box.createHorizontalStrut(14));
        derecha.add(btnNuevo);

        JPanel encabezado = Estilo.transparente(new BorderLayout());
        encabezado.add(resumenCentrado, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);
        return encabezado;
    }

    private JComponent armarTabla() {
        orden.setSortKeys(List.of(new RowSorter.SortKey(StockTableModel.COL_INSUMO, SortOrder.ASCENDING)));
        tabla.setRowSorter(orden);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Tablas.estilizar(tabla, ALTO_FILA);

        Font normal = Estilo.fuente(Font.PLAIN, 16f);
        Font negrita = Estilo.fuente(Font.BOLD, 16f);
        Tablas.renderer(tabla, 0, new Tablas.RendererTexto(negrita, Estilo.TEXTO, String::valueOf));
        Tablas.renderer(tabla, 1, new Tablas.RendererTexto(normal, Estilo.TEXTO,
                v -> v.toString().isBlank() ? "—" : v.toString()));
        Tablas.renderer(tabla, 2, new Tablas.RendererTexto(normal, Estilo.TEXTO_SUAVE, String::valueOf));
        Tablas.renderer(tabla, 3, new RendererCantidad(negrita));
        Tablas.renderer(tabla, 4, new Tablas.RendererTexto(normal, Estilo.TEXTO_SUAVE,
                v -> Tablas.numero(((Number) v).doubleValue())));
        Tablas.renderer(tabla, 5, new Tablas.RendererInsignia(v -> null) {
            @Override
            protected Tablas.Insignia insigniaPara(JTable t, Object valor, int filaVista) {
                Stock s = modelo.getFila(t.convertRowIndexToModel(filaVista));
                if (s.alcanzoStockMinimo()) {
                    return Tablas.Insignia.roja("Bajo mínimo");
                }
                return s.isStockMinimoActivo() ? Tablas.Insignia.verde("Alerta activa")
                        : Tablas.Insignia.gris("Sin alerta");
            }
        });
        Tablas.anchos(tabla, 260, 200, 120, 150, 130, 180);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(Tablas.scroll(tabla), BorderLayout.CENTER);

        // la tarjeta mide lo que ocupan las filas (como mucho, el alto disponible)
        JPanel contenedor = Estilo.transparente(new Tablas.Apilado(0, () -> Tablas.altoContenido(tabla)));
        contenedor.add(tarjeta);
        return contenedor;
    }

    private JComponent armarAcciones() {
        List<JComponent> botones = new ArrayList<>();
        if (Sesion.esAdmin()) {
            botones.add(btnEliminar);
        }
        botones.add(btnModificar);
        botones.add(btnAjustar);
        botones.add(btnReponer);
        JLabel ayuda = new JLabel("Reponer suma lo que llegó; Ajustar corrige la cantidad");
        ayuda.setFont(Estilo.fuente(Font.PLAIN, 15f));
        ayuda.setForeground(Estilo.TEXTO_SUAVE);
        return Tablas.barraAcciones(ayuda, botones.toArray(new JComponent[0]));
    }

    // ---------- lo que usa el controlador ----------

    public void mostrarInsumos(List<Stock> insumos) {
        modelo.setFilas(insumos);
        aplicarFiltro();
    }

    public Optional<Stock> insumoSeleccionado() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return Optional.empty();
        }
        return Optional.of(modelo.getFila(tabla.convertRowIndexToModel(filaVista)));
    }

    /** Deja seleccionado el insumo que se acaba de tocar (después de recargar). */
    public void seleccionarInsumo(int idStock) {
        for (int fila = 0; fila < tabla.getRowCount(); fila++) {
            if (modelo.getFila(tabla.convertRowIndexToModel(fila)).getIdStock() == idStock) {
                tabla.setRowSelectionInterval(fila, fila);
                tabla.scrollRectToVisible(tabla.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    public void habilitarAcciones(boolean hayInsumo) {
        btnModificar.setEnabled(hayInsumo);
        btnReponer.setEnabled(hayInsumo);
        btnAjustar.setEnabled(hayInsumo);
        btnEliminar.setEnabled(hayInsumo);
    }

    public JButton getBotonActualizar() { return btnActualizar; }
    public JButton getBotonReponer() { return btnReponer; }
    public JButton getBotonAjustar() { return btnAjustar; }
    public JButton getBotonEliminar() { return btnEliminar; }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public boolean confirmar(String pregunta) {
        return Dialogos.confirmar(this, pregunta);
    }

    // ---------- filtro ----------

    private void aplicarFiltro() {
        orden.setRowFilter(chkSoloBajoMinimo.isSelected() ? new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends StockTableModel, ? extends Integer> fila) {
                return fila.getModel().getFila(fila.getIdentifier()).alcanzoStockMinimo();
            }
        } : null);

        int bajoMinimo = 0;
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (modelo.getFila(i).alcanzoStockMinimo()) {
                bajoMinimo++;
            }
        }
        lblResumen.setText(modelo.getRowCount() + (modelo.getRowCount() == 1 ? " insumo" : " insumos"));
        lblResumenDetalle.setForeground(bajoMinimo > 0 ? Estilo.ROJO_TEXTO : Estilo.TEXTO_SUAVE);
        lblResumenDetalle.setText(" · " + (bajoMinimo == 0 ? "ninguno bajo mínimo" : bajoMinimo + " bajo mínimo"));
        habilitarAcciones(insumoSeleccionado().isPresent());
        firePropertyChange(PROP_BAJO_MINIMO, -1, bajoMinimo);
        revalidate();
        repaint();
    }

    /** Cantidad actual: en rojo si el insumo está bajo mínimo. */
    private class RendererCantidad extends Tablas.RendererTexto {
        RendererCantidad(Font fuente) {
            super(fuente, Estilo.TEXTO, v -> Tablas.numero(((Number) v).doubleValue()));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(t, valor, seleccionada, foco, fila, columna);
            if (modelo.getFila(t.convertRowIndexToModel(fila)).alcanzoStockMinimo()) {
                setForeground(Estilo.ROJO_TEXTO);
            }
            return this;
        }
    }
}
