package org.example.escritorio.vista.cocina;

import org.example.Modelo.menu.Menu;
import org.example.escritorio.controlador.cocina.MenuControlador;
import org.example.escritorio.sesion.Sesion;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Icono;
import org.example.escritorio.vista.comun.Tablas;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * La carta del restaurante (RF-26 a RF-28): alta y modificación de platos y su receta.
 * Eliminar un plato es solo para ADMIN (RF-27): el botón no aparece para los demás.
 */
public class MenuPanel extends JPanel {

    private static final int ALTO_FILA = 58;

    private final MenuTableModel modelo = new MenuTableModel();
    private final JTable tabla = new JTable(modelo);

    private final JButton btnActualizar = new Boton(null, new Icono(Icono.Tipo.ACTUALIZAR, 20, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);
    private final JButton btnNuevo = new Boton("Nuevo plato", new Icono(Icono.Tipo.MAS, 16, Color.WHITE),
            Boton.Tipo.PRIMARIO);
    private final JButton btnModificar = new Boton("Modificar", Boton.Tipo.OSCURO);
    private final JButton btnReceta = new Boton("Receta", Boton.Tipo.CONTORNO);
    private final JButton btnEliminar = new Boton("Eliminar", Boton.Tipo.CONTORNO_CAFE);
    private final JLabel lblResumen = new JLabel(" ");
    private final JLabel lblResumenDetalle = new JLabel(" ");

    private final MenuControlador controlador;

    public MenuPanel() {
        super(new BorderLayout(0, 22));
        setOpaque(false);
        setBorder(Estilo.relleno(4, 0, 0, 0));

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

        btnActualizar.setToolTipText("Actualizar");
        btnActualizar.setBorder(Estilo.relleno(12, 12, 12, 12));

        JPanel derecha = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(btnActualizar);
        derecha.add(Box.createHorizontalStrut(14));
        derecha.add(btnNuevo);

        JPanel encabezado = Estilo.transparente(new BorderLayout());
        encabezado.add(resumenCentrado, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);
        return encabezado;
    }

    private JComponent armarTabla() {
        TableRowSorter<MenuTableModel> orden = new TableRowSorter<>(modelo);
        orden.setSortKeys(List.of(new RowSorter.SortKey(MenuTableModel.COL_NOMBRE, SortOrder.ASCENDING)));
        tabla.setRowSorter(orden);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Tablas.estilizar(tabla, ALTO_FILA);

        Font normal = Estilo.fuente(Font.PLAIN, 16f);
        Font negrita = Estilo.fuente(Font.BOLD, 16f);
        Tablas.renderer(tabla, 0, new Tablas.RendererTexto(negrita, Estilo.TEXTO, String::valueOf));
        Tablas.renderer(tabla, 1, new Tablas.RendererTexto(normal, Estilo.TEXTO_SUAVE,
                v -> v.toString().isBlank() ? "—" : v.toString()));
        Tablas.renderer(tabla, 2, new Tablas.RendererTexto(normal, Estilo.TEXTO,
                v -> Tablas.pesosCortos((BigDecimal) v)));
        Tablas.renderer(tabla, 3, new Tablas.RendererInsignia(v -> Boolean.TRUE.equals(v)
                ? Tablas.Insignia.verde("Disponible") : Tablas.Insignia.gris("No disponible")));
        Tablas.anchos(tabla, 260, 420, 140, 170);

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
        botones.add(btnReceta);
        botones.add(btnModificar);
        JLabel ayuda = new JLabel("Doble clic en un plato para modificarlo");
        ayuda.setFont(Estilo.fuente(Font.PLAIN, 15f));
        ayuda.setForeground(Estilo.TEXTO_SUAVE);
        return Tablas.barraAcciones(ayuda, botones.toArray(new JComponent[0]));
    }

    // ---------- lo que usa el controlador ----------

    public void mostrarPlatos(List<Menu> platos) {
        modelo.setFilas(platos);
        long disponibles = platos.stream().filter(Menu::isDisponible).count();
        lblResumen.setText(platos.size() + (platos.size() == 1 ? " plato" : " platos"));
        lblResumenDetalle.setText(" · " + disponibles + (disponibles == 1 ? " disponible" : " disponibles"));
        habilitarAcciones(platoSeleccionado().isPresent());
        revalidate();
        repaint();
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
