package org.example.escritorio.vista.cocina;

import org.example.Modelo.inventario.Stock;
import org.example.Modelo.menu.Menu;
import org.example.Modelo.menu.MenuIngrediente;
import org.example.escritorio.controlador.cocina.RecetaControlador;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.DialogoFormulario;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formulario;
import org.example.escritorio.vista.comun.Tablas;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

/**
 * Receta de un plato (RF-28): qué insumos lleva y cuánto por porción.
 * Es lo que PedidoService usa para descontar stock cuando se carga un pedido (RF-29).
 * La acción principal es agregar un insumo; la ventana queda abierta hasta tocar Cerrar.
 */
public class RecetaDialog extends DialogoFormulario {

    private static final int ALTO_FILA = 48;

    private final Menu plato;

    private final RecetaTableModel modelo = new RecetaTableModel();
    private final JTable tabla = new JTable(modelo);

    private final JComboBox<Stock> cmbInsumo = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(0.1, 0.001, 100_000d, 0.1));
    private final JLabel lblUnidad = Formulario.nota(new JLabel(" "));
    private final JButton btnCambiarCantidad = new Boton("Cambiar cantidad", Boton.Tipo.CONTORNO);
    private final JButton btnQuitar = new Boton("Quitar", Boton.Tipo.CONTORNO_CAFE);

    private final RecetaControlador controlador;

    public RecetaDialog(Window padre, Menu plato) {
        super(padre, "Cocina · Receta", plato.getNombre(),
                "Cantidades por porción: al cargar un pedido se descuenta cantidad × unidades pedidas.",
                "Agregar insumo");
        this.plato = plato;
        btnCancelar.setText("Cerrar");

        spnCantidad.setEditor(new JSpinner.NumberEditor(spnCantidad, "0.###"));
        cmbInsumo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof Stock s) {
                    setText(s.getNombre() + " (" + s.getUnidadMedida() + ")");
                }
                return this;
            }
        });

        controlador = new RecetaControlador(this);

        cmbInsumo.addActionListener(e -> {
            Stock s = getInsumoElegido();
            lblUnidad.setText(s == null ? " " : s.getUnidadMedida());
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                habilitarAccionesFila(ingredienteSeleccionado().isPresent());
            }
        });
        btnPrincipal.addActionListener(e -> controlador.agregar());
        btnCambiarCantidad.addActionListener(e -> controlador.cambiarCantidad());
        btnQuitar.addActionListener(e -> controlador.quitar());

        habilitarAccionesFila(false);
        controlador.cargar();

        armar();
    }

    @Override
    protected JComponent armarCuerpo() {
        return Formulario.cuerpo(armarTabla(), armarAgregar());
    }

    /** La receta actual en una tarjeta, con Quitar y Cambiar cantidad arriba a la derecha. */
    private JComponent armarTabla() {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Tablas.estilizar(tabla, ALTO_FILA);
        Font normal = Estilo.fuente(Font.PLAIN, 15f);
        Font negrita = Estilo.fuente(Font.BOLD, 15f);
        Tablas.renderer(tabla, 0, new Tablas.RendererTexto(negrita, Estilo.TEXTO, String::valueOf));
        Tablas.renderer(tabla, 1, new Tablas.RendererTexto(normal, Estilo.TEXTO,
                v -> Tablas.numero(((Number) v).doubleValue())));
        Tablas.renderer(tabla, 2, new Tablas.RendererTexto(normal, Estilo.TEXTO_SUAVE, String::valueOf));
        Tablas.renderer(tabla, 3, new Tablas.RendererTexto(normal, Estilo.TEXTO_SUAVE,
                v -> Tablas.numero(((Number) v).doubleValue())));
        Tablas.anchos(tabla, 170, 240, 100, 140);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 18);
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(Tablas.scroll(tabla), BorderLayout.CENTER);
        tarjeta.setPreferredSize(new Dimension(650, 260));

        JLabel titulo = new JLabel("Insumos de la receta");
        titulo.setFont(Estilo.fuente(Font.BOLD, 15f));
        titulo.setForeground(Estilo.TEXTO);
        btnQuitar.setBorder(Estilo.relleno(8, 16, 8, 16));
        btnCambiarCantidad.setBorder(Estilo.relleno(8, 16, 8, 16));
        JPanel botones = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnQuitar);
        botones.add(btnCambiarCantidad);
        JPanel arriba = Estilo.transparente(new BorderLayout());
        arriba.add(titulo, BorderLayout.WEST);
        arriba.add(botones, BorderLayout.EAST);

        JPanel panel = Estilo.transparente(new BorderLayout(0, 10));
        panel.add(arriba, BorderLayout.NORTH);
        panel.add(tarjeta, BorderLayout.CENTER);
        return panel;
    }

    /** Insumo y cantidad para sumar a la receta; se agrega con el botón principal. */
    private JComponent armarAgregar() {
        Formulario.estilizar(cmbInsumo);
        Formulario.estilizar(spnCantidad);

        JPanel cantidad = Estilo.transparente(new BorderLayout(10, 0));
        cantidad.add(spnCantidad, BorderLayout.CENTER);
        cantidad.add(lblUnidad, BorderLayout.EAST);

        JPanel fila = Estilo.transparente(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 0.65;
        c.insets = new Insets(0, 0, 0, 16);
        fila.add(Formulario.campo("Insumo a agregar", cmbInsumo, null), c);
        c.weightx = 0.35;
        c.insets = new Insets(0, 0, 0, 0);
        fila.add(Formulario.campo("Cantidad por porción", cantidad, null), c);

        JPanel caja = Estilo.transparente(new BorderLayout());
        caja.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDE),
                Estilo.relleno(18, 0, 0, 0)));
        caja.add(fila, BorderLayout.CENTER);
        return caja;
    }

    // ---------- lo que usa el controlador ----------

    public Menu getPlato() { return plato; }
    public Stock getInsumoElegido() { return (Stock) cmbInsumo.getSelectedItem(); }

    /** La cantidad escrita; null si no es un número válido. */
    public Double getCantidad() {
        try {
            spnCantidad.commitEdit();
        } catch (ParseException e) {
            return null;
        }
        return ((Number) spnCantidad.getValue()).doubleValue();
    }

    public JButton getBotonAgregar() { return btnPrincipal; }
    public JButton getBotonCambiarCantidad() { return btnCambiarCantidad; }
    public JButton getBotonQuitar() { return btnQuitar; }
    public JTable getTabla() { return tabla; }

    /** Muestra la receta y, en el combo, solo los insumos que todavía no están en ella. */
    public void mostrarReceta(List<MenuIngrediente> ingredientes, List<Stock> insumosParaAgregar) {
        modelo.setFilas(ingredientes);
        cmbInsumo.removeAllItems();
        insumosParaAgregar.forEach(cmbInsumo::addItem);
        btnPrincipal.setEnabled(!insumosParaAgregar.isEmpty());
        spnCantidad.setValue(0.1);
        habilitarAccionesFila(ingredienteSeleccionado().isPresent());
    }

    public Optional<MenuIngrediente> ingredienteSeleccionado() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? Optional.empty() : Optional.of(modelo.getFila(tabla.convertRowIndexToModel(fila)));
    }

    public void habilitarAccionesFila(boolean hayIngrediente) {
        btnCambiarCantidad.setEnabled(hayIngrediente);
        btnQuitar.setEnabled(hayIngrediente);
    }

    public boolean confirmar(String pregunta) {
        return Dialogos.confirmar(this, pregunta);
    }
}
