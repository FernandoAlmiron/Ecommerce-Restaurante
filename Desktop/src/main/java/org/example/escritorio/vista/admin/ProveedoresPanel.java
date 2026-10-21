package org.example.escritorio.vista.admin;

import org.example.Modelo.inventario.Categoria;
import org.example.Modelo.inventario.Proveedores;
import org.example.escritorio.controlador.admin.ProveedoresControlador;
import org.example.escritorio.vista.comun.Avatar;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formulario;
import org.example.escritorio.vista.comun.Icono;
import org.example.escritorio.vista.comun.PanelAbm;
import org.example.escritorio.vista.comun.Tablas;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Solapa Proveedores de Administración (RF-56).
 * Arriba se busca por razón social o CUIT y se filtra por categoría; a la derecha, el detalle del elegido.
 */
public class ProveedoresPanel extends PanelAbm<Proveedores> {

    private static final String TODAS = "Todas";

    private final JTextField txtBuscar = new JTextField(16);
    private final JComboBox<String> cmbCategoria = new JComboBox<>(new String[]{TODAS});

    // detalle
    private final Avatar avatar = new Avatar(68, 22f);
    private final JLabel lblNombre = new JLabel(" ");
    private final Estilo.Pildora lblCategoria = new Estilo.Pildora("", Color.WHITE, Estilo.TEXTO,
            Estilo.fuente(Font.BOLD, 14f), -1);
    private final JLabel lblCuit = valor();
    private final JLabel lblTelefono = valor();
    private final JLabel lblEmail = valor();
    private final JButton btnCopiar = new Boton("Copiar teléfono", new Icono(Icono.Tipo.TELEFONO, 16, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);
    private final JButton btnEmail = new Boton("Enviar email", new Icono(Icono.Tipo.SOBRE, 16, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);
    private final JPanel cuerpoDetalle = Estilo.transparente(new BorderLayout(0, 18));
    private Proveedores mostrado;

    public ProveedoresPanel() {
        super("Nuevo proveedor", 1, Disposicion.LISTA);
        ProveedoresControlador controlador = new ProveedoresControlador(this);

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refiltrar(); }
            @Override public void removeUpdate(DocumentEvent e) { refiltrar(); }
            @Override public void changedUpdate(DocumentEvent e) { refiltrar(); }
        });
        cmbCategoria.addActionListener(e -> refiltrar());
        btnCopiar.addActionListener(e -> copiarTelefono());
        btnEmail.addActionListener(e -> enviarEmail());

        conectar(() -> controlador.cargar(null), controlador::nuevo, controlador::modificar,
                controlador::eliminar, controlador::alCambiarSeleccion);
        controlador.cargar(null);
    }

    @Override
    protected Comparator<Proveedores> orden() {
        return Comparator.comparing(Proveedores::getRazonSocial, String.CASE_INSENSITIVE_ORDER);
    }

    @Override
    protected boolean mismo(Proveedores a, Proveedores b) {
        return a.getNroProveedor() == b.getNroProveedor();
    }

    // ---------- filtros ----------

    @Override
    protected JComponent armarFiltros() {
        Formulario.estilizar(txtBuscar, "Buscar por razón social o CUIT");
        txtBuscar.putClientProperty("JTextField.leadingIcon", new Icono(Icono.Tipo.LUPA, 18, Estilo.TEXTO_SUAVE));
        Formulario.estilizar(cmbCategoria);
        cmbCategoria.setPreferredSize(new java.awt.Dimension(260, cmbCategoria.getPreferredSize().height));
        cmbCategoria.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> lista, Object valor,
                                                                   int indice, boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                setText("Categoría   " + valor);
                return this;
            }
        });
        JPanel filtros = Estilo.transparente(new BorderLayout(14, 0));
        filtros.add(txtBuscar, BorderLayout.CENTER);
        filtros.add(cmbCategoria, BorderLayout.EAST);
        return filtros;
    }

    @Override
    protected boolean incluir(Proveedores p) {
        String texto = txtBuscar.getText().trim().toLowerCase();
        String soloNumeros = texto.replaceAll("[-.\\s]", "");
        boolean coincideTexto = texto.isEmpty()
                || (p.getRazonSocial() != null && p.getRazonSocial().toLowerCase().contains(texto))
                || (!soloNumeros.isEmpty() && p.getCuit() != null && p.getCuit().contains(soloNumeros));
        String categoria = (String) cmbCategoria.getSelectedItem();
        boolean coincideCategoria = categoria == null || TODAS.equals(categoria)
                || categoria.equals(nombreCategoria(p));
        return coincideTexto && coincideCategoria;
    }

    /** El filtro de categorías se arma con las que tienen proveedores; se mantiene la elegida. */
    @Override
    public void mostrar(List<Proveedores> nuevas, String resumen) {
        String elegida = (String) cmbCategoria.getSelectedItem();
        DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<>();
        modelo.addElement(TODAS);
        nuevas.stream().map(ProveedoresPanel::nombreCategoria).filter(Objects::nonNull)
                .distinct().sorted(String.CASE_INSENSITIVE_ORDER).forEach(modelo::addElement);
        cmbCategoria.setModel(modelo);
        cmbCategoria.setSelectedItem(modelo.getIndexOf(elegida) >= 0 ? elegida : TODAS);
        super.mostrar(nuevas, resumen);
    }

    // ---------- filas ----------

    @Override
    protected JComponent crearFila(Proveedores p, boolean elegida) {
        Avatar iniciales = new Avatar(52, 17f, p.getRazonSocial());
        iniciales.setColores(elegida ? Estilo.CAFE : Estilo.FILA_SELECCIONADA, elegida ? Color.WHITE : Estilo.CAFE);

        JLabel nombre = new JLabel(p.getRazonSocial());
        nombre.setFont(Estilo.fuente(Font.BOLD, 18f));
        nombre.setForeground(Estilo.TEXTO);
        nombre.setBorder(Estilo.relleno(0, 0, 0, 6));
        JLabel cuit = new JLabel("CUIT " + ProveedorTableModel.cuit(p.getCuit()));
        cuit.setFont(Estilo.fuente(Font.PLAIN, 15f));
        cuit.setForeground(Estilo.TEXTO_SUAVE);
        JPanel textos = Estilo.transparente(null);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(nombre);
        textos.add(Box.createVerticalStrut(2));
        textos.add(cuit);

        JPanel categoria = Estilo.transparente(new GridBagLayout());
        categoria.add(pildoraCategoria(p.getCategoria()));

        JPanel fila = Estilo.transparente(new BorderLayout(18, 0));
        fila.add(iniciales, BorderLayout.WEST);
        fila.add(textos, BorderLayout.CENTER);
        fila.add(categoria, BorderLayout.EAST);
        return fila;
    }

    /** Cada categoría con un color fijo (según su número), igual en la lista y en el detalle. */
    private static Estilo.Pildora pildoraCategoria(Categoria categoria) {
        String nombre = categoria == null ? "Sin categoría" : categoria.getNombre();
        Tablas.Insignia colores = categoria == null ? Tablas.Insignia.gris(nombre)
                : switch (Math.floorMod(categoria.getNroCategoria(), 6)) {
                    case 0 -> Tablas.Insignia.amarilla(nombre);
                    case 1 -> Tablas.Insignia.roja(nombre);
                    case 2 -> Tablas.Insignia.azul(nombre);
                    case 3 -> Tablas.Insignia.violeta(nombre);
                    case 4 -> Tablas.Insignia.verde(nombre);
                    default -> Tablas.Insignia.gris(nombre);
                };
        Estilo.Pildora pildora = new Estilo.Pildora(colores.texto(), colores.fondo(), colores.color(),
                Estilo.fuente(Font.BOLD, 14f), -1);
        pildora.setBorder(Estilo.relleno(5, 14, 5, 14));
        return pildora;
    }

    private static String nombreCategoria(Proveedores p) {
        return p.getCategoria() == null ? null : p.getCategoria().getNombre();
    }

    // ---------- detalle ----------

    @Override
    protected JComponent armarDetalle() {
        avatar.setColores(Estilo.CAFE, Color.WHITE);
        lblNombre.setFont(Estilo.fuente(Font.BOLD, 22f));
        lblNombre.setForeground(Estilo.TEXTO);
        lblNombre.setBorder(Estilo.relleno(0, 0, 0, 6));
        lblCategoria.setBorder(Estilo.relleno(4, 12, 4, 12));
        JPanel filaCategoria = Estilo.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaCategoria.add(lblCategoria);
        JPanel titulos = Estilo.transparente(null);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        lblNombre.setAlignmentX(LEFT_ALIGNMENT);
        filaCategoria.setAlignmentX(LEFT_ALIGNMENT);
        titulos.add(lblNombre);
        titulos.add(Box.createVerticalStrut(6));
        titulos.add(filaCategoria);
        JPanel titulosCentrados = Estilo.transparente(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.weightx = 1;
        titulosCentrados.add(titulos, g);

        JPanel cabecera = new JPanel(new BorderLayout(18, 0));
        cabecera.setBackground(Estilo.FILA_SELECCIONADA);
        cabecera.setBorder(Estilo.relleno(24, 26, 22, 26));
        cabecera.add(avatar, BorderLayout.WEST);
        cabecera.add(titulosCentrados, BorderLayout.CENTER);

        JPanel datos = Estilo.transparente(null);
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));
        datos.add(fila(Icono.Tipo.DOCUMENTO, "CUIT", lblCuit));
        datos.add(fila(Icono.Tipo.TELEFONO, "Teléfono", lblTelefono));
        datos.add(fila(Icono.Tipo.SOBRE, "Email", lblEmail));

        btnCopiar.setBorder(Estilo.relleno(12, 14, 12, 14));
        btnEmail.setBorder(Estilo.relleno(12, 14, 12, 14));
        JPanel contacto = Estilo.transparente(new GridLayout(1, 2, 10, 0));
        contacto.add(btnCopiar);
        contacto.add(btnEmail);

        JPanel acciones = Estilo.transparente(new BorderLayout(0, 12));
        acciones.add(contacto, BorderLayout.NORTH);
        acciones.add(botonesDetalle(), BorderLayout.CENTER);

        cuerpoDetalle.setBorder(Estilo.relleno(6, 26, 24, 26));
        cuerpoDetalle.add(datos, BorderLayout.NORTH);
        cuerpoDetalle.add(acciones, BorderLayout.CENTER);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(cabecera, BorderLayout.NORTH);
        tarjeta.add(cuerpoDetalle, BorderLayout.CENTER);
        return tarjeta;
    }

    @Override
    protected void mostrarDetalle(Proveedores p) {
        mostrado = p;
        if (p == null) {
            avatar.setNombre("");
            lblNombre.setText("Elegí un proveedor");
            lblCategoria.setVisible(false);
            cuerpoDetalle.setVisible(false);
            return;
        }
        cuerpoDetalle.setVisible(true);
        avatar.setNombre(p.getRazonSocial());
        lblNombre.setText(p.getRazonSocial());
        lblCategoria.setText(nombreCategoria(p) == null ? "Sin categoría" : nombreCategoria(p));
        lblCategoria.setVisible(true);
        lblCuit.setText(ProveedorTableModel.cuit(p.getCuit()));
        lblTelefono.setText(vacio(p.getTelefono()) ? "—" : p.getTelefono());
        lblEmail.setText(vacio(p.getEmail()) ? "—" : p.getEmail());
        btnCopiar.setEnabled(!vacio(p.getTelefono()));
        btnEmail.setEnabled(!vacio(p.getEmail()));
    }

    private static JLabel valor() {
        JLabel l = new JLabel("—");
        l.setFont(Estilo.fuente(Font.BOLD, 16f));
        l.setForeground(Estilo.TEXTO);
        l.setBorder(Estilo.relleno(0, 0, 0, 4)); // la negrita no tiene que quedar recortada
        return l;
    }

    /** "[ícono] CUIT ........ 30-71122334-4" con una línea finita abajo. */
    private static JComponent fila(Icono.Tipo icono, String nombre, JLabel valor) {
        JLabel lbl = new JLabel(nombre, new Icono(icono, 18, Estilo.TEXTO_SUAVE), JLabel.LEFT);
        lbl.setIconTextGap(12);
        lbl.setFont(Estilo.fuente(Font.PLAIN, 16f));
        lbl.setForeground(Estilo.TEXTO_SUAVE);
        JPanel fila = Estilo.transparente(new BorderLayout(12, 0));
        fila.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE), Estilo.relleno(14, 0, 14, 0)));
        fila.add(lbl, BorderLayout.WEST);
        fila.add(valor, BorderLayout.EAST);
        fila.setAlignmentX(LEFT_ALIGNMENT);
        return fila;
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    // ---------- contacto ----------

    private void copiarTelefono() {
        if (mostrado != null && !vacio(mostrado.getTelefono())) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(mostrado.getTelefono()), null);
            Dialogos.info(this, "Se copió el teléfono " + mostrado.getTelefono() + ".");
        }
    }

    /** Abre el programa de correo de la computadora con el email del proveedor. */
    private void enviarEmail() {
        if (mostrado == null || vacio(mostrado.getEmail())) {
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
                Desktop.getDesktop().mail(new URI("mailto", mostrado.getEmail(), null));
            } else {
                mostrarError("No se encontró un programa de correo. El email es " + mostrado.getEmail() + ".");
            }
        } catch (Exception e) {
            mostrarError("No se pudo abrir el correo: " + e.getMessage());
        }
    }
}
