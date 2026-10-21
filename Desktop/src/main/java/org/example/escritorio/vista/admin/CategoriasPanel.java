package org.example.escritorio.vista.admin;

import org.example.Modelo.inventario.Categoria;
import org.example.escritorio.controlador.admin.CategoriasControlador;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.PanelAbm;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Comparator;

/**
 * Solapa Categorías de Administración (RF-56): las categorías que usan el stock y los proveedores.
 * La categoría elegida se edita ahí mismo, en el panel de la derecha.
 */
public class CategoriasPanel extends PanelAbm<Categoria> {

    private final JLabel lblEditar = new JLabel("EDITAR CATEGORÍA");
    private final JLabel lblNombre = new JLabel(" ");
    private final JPanel lugarFormulario = Estilo.transparente(new BorderLayout());
    private final JPanel cuerpoDetalle = Estilo.transparente(new BorderLayout(0, 22));
    private final JButton btnGuardar = new Boton("Guardar cambios", Boton.Tipo.PRIMARIO);
    private CategoriaForm formulario;

    public CategoriasPanel() {
        super("Nueva categoría", 1, Disposicion.LISTA);
        CategoriasControlador controlador = new CategoriasControlador(this);
        btnGuardar.addActionListener(e -> controlador.guardarCambios());
        conectar(() -> controlador.cargar(null), controlador::nuevo, controlador::modificar,
                controlador::eliminar, controlador::alCambiarSeleccion);
        controlador.cargar(null);
    }

    @Override
    protected Comparator<Categoria> orden() {
        return Comparator.comparing(Categoria::getNombre, String.CASE_INSENSITIVE_ORDER);
    }

    @Override
    protected boolean mismo(Categoria a, Categoria b) {
        return a.getNroCategoria() == b.getNroCategoria();
    }

    @Override
    protected double pesoDetalle() {
        return 0.38;
    }

    @Override
    protected JComponent crearFila(Categoria categoria, boolean elegida) {
        JLabel nombre = new JLabel(categoria.getNombre());
        nombre.setFont(Estilo.fuente(Font.BOLD, 18f));
        nombre.setForeground(Estilo.TEXTO);
        nombre.setBorder(Estilo.relleno(0, 0, 0, 6));
        String descripcion = categoria.getDescripcion() == null || categoria.getDescripcion().isBlank()
                ? "Sin descripción" : categoria.getDescripcion();
        JLabel lblDescripcion = new JLabel(descripcion);
        lblDescripcion.setFont(Estilo.fuente(Font.PLAIN, 15f));
        lblDescripcion.setForeground(Estilo.TEXTO_SUAVE);
        JPanel textos = Estilo.transparente(null);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(nombre);
        textos.add(Box.createVerticalStrut(2));
        textos.add(lblDescripcion);

        Estilo.Pildora estado = categoria.isActivo()
                ? new Estilo.Pildora("Activa", Estilo.VERDE_FONDO, Estilo.VERDE_TEXTO, Estilo.fuente(Font.BOLD, 14f), -1)
                : new Estilo.Pildora("Inactiva", Estilo.GRIS_FONDO, Estilo.GRIS_TEXTO, Estilo.fuente(Font.BOLD, 14f), -1);
        estado.setBorder(Estilo.relleno(5, 14, 5, 14));
        JPanel estadoCentrado = Estilo.transparente(new java.awt.GridBagLayout());
        estadoCentrado.add(estado);

        JPanel fila = Estilo.transparente(new BorderLayout(18, 0));
        fila.add(new Inicial(categoria.getNombre(), elegida), BorderLayout.WEST);
        fila.add(textos, BorderLayout.CENTER);
        fila.add(estadoCentrado, BorderLayout.EAST);
        return fila;
    }

    // ---------- detalle: la categoría elegida, para editar ----------

    @Override
    protected JComponent armarDetalle() {
        lblEditar.setFont(Estilo.conEspaciado(Estilo.fuente(Font.BOLD, 13f), 0.1f));
        lblEditar.setForeground(Estilo.CAFE);
        lblEditar.setBorder(Estilo.relleno(0, 0, 0, 8));
        lblNombre.setFont(Estilo.fuente(Font.BOLD, 24f));
        lblNombre.setForeground(Estilo.TEXTO);
        lblNombre.setBorder(Estilo.relleno(0, 0, 0, 6));
        JPanel titulos = Estilo.transparente(null);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        titulos.add(lblEditar);
        titulos.add(Box.createVerticalStrut(6));
        titulos.add(lblNombre);

        cuerpoDetalle.add(lugarFormulario, BorderLayout.CENTER);
        cuerpoDetalle.add(botonesDetalle(btnGuardar), BorderLayout.SOUTH);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(0, 22), 22);
        tarjeta.setBorder(Estilo.relleno(26, 28, 28, 28));
        tarjeta.add(titulos, BorderLayout.NORTH);
        tarjeta.add(cuerpoDetalle, BorderLayout.CENTER);
        return tarjeta;
    }

    @Override
    protected void mostrarDetalle(Categoria categoria) {
        lugarFormulario.removeAll();
        if (categoria == null) {
            formulario = null;
            lblEditar.setText("CATEGORÍAS");
            lblNombre.setText("Elegí una categoría");
            cuerpoDetalle.setVisible(false);
        } else {
            formulario = new CategoriaForm(categoria);
            lblEditar.setText("EDITAR CATEGORÍA");
            lblNombre.setText(categoria.getNombre());
            lugarFormulario.add(formulario, BorderLayout.CENTER);
            cuerpoDetalle.setVisible(true);
        }
        lugarFormulario.revalidate();
        lugarFormulario.repaint();
    }

    // ---------- lo que usa el controlador ----------

    /** Los campos de la categoría elegida, como quedaron en el panel de la derecha (null si no hay ninguna). */
    public CategoriaForm getFormularioEdicion() {
        return formulario;
    }

    public JButton getBotonGuardar() {
        return btnGuardar;
    }

    /** La inicial de la categoría en un cuadradito (café si está elegida). */
    private static class Inicial extends JComponent {
        private final String letra;
        private final boolean elegida;

        Inicial(String nombre, boolean elegida) {
            this.letra = nombre == null || nombre.isBlank() ? "?" : nombre.substring(0, 1).toUpperCase();
            this.elegida = elegida;
            setPreferredSize(new Dimension(52, 52));
            setFont(Estilo.fuente(Font.BOLD, 18f));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Estilo.suave(g);
            int lado = Math.min(getWidth(), getHeight());
            int y = (getHeight() - lado) / 2;
            g2.setColor(elegida ? Estilo.CAFE : Estilo.FILA_SELECCIONADA);
            g2.fillRoundRect(0, y, lado, lado, 14, 14);
            g2.setColor(elegida ? Color.WHITE : Estilo.CAFE);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(letra, (lado - fm.stringWidth(letra)) / 2, y + (lado - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
