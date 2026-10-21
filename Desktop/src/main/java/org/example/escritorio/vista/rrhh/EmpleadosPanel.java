package org.example.escritorio.vista.rrhh;

import org.example.Modelo.persona.Empleado;
import org.example.escritorio.controlador.rrhh.EmpleadosControlador;
import org.example.escritorio.vista.comun.Avatar;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formulario;
import org.example.escritorio.vista.comun.Icono;
import org.example.escritorio.vista.comun.Selector;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Recursos humanos (RF-53 a RF-55): alta, modificación, cambio de sector y baja de empleados.
 * A la izquierda la lista con buscador y filtro por sector; a la derecha el detalle del elegido.
 * Los empleados de ADMINISTRACION solo los puede tocar un ADMIN: para RRHH esos botones quedan deshabilitados.
 */
public class EmpleadosPanel extends JPanel {

    private static final int ALTO_FILA = 64;
    /** Sectores que entran como opción directa del filtro; el resto va en "Más sectores". */
    private static final int SECTORES_A_LA_VISTA = 3;
    private static final String MAS_SECTORES = "Más sectores";

    private final EmpleadoTableModel modelo = new EmpleadoTableModel();
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<EmpleadoTableModel> orden = new TableRowSorter<>(modelo);

    private final JTextField txtBuscar = new JTextField(16);
    private final Selector selSector = new Selector("Todos");
    private final JComboBox<String> cmbMasSectores = new JComboBox<>();
    private final JButton btnActualizar = new Boton(null, new Icono(Icono.Tipo.ACTUALIZAR, 20, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);
    private final JButton btnNuevo = new Boton("Nuevo empleado", new Icono(Icono.Tipo.MAS, 16, Color.WHITE),
            Boton.Tipo.PRIMARIO);
    private final JLabel lblResumen = new JLabel(" ");
    private final JLabel lblResumenDetalle = new JLabel(" ");

    /** Sector de cada opción del selector (la primera, "Todos", es null). */
    private final List<String> sectoresDelSelector = new ArrayList<>();
    /** Sector por el que se filtra; null = todos. */
    private String sectorFiltro;
    private boolean armandoFiltro;

    // detalle del empleado elegido
    private final Avatar avatarDetalle = new Avatar(84, 28f);
    private final JLabel lblNombre = new JLabel(" ", SwingConstants.CENTER);
    private final Estilo.Pildora lblSector = new Estilo.Pildora("", Color.WHITE, Estilo.TEXTO,
            Estilo.fuente(Font.BOLD, 14f), -1);
    private final JLabel lblLegajo = new JLabel(" ");
    private final JLabel lblDni = valor();
    private final JLabel lblCelular = valor();
    private final JLabel lblUsuario = valor();
    private final Estilo.Pildora lblSectorFila = new Estilo.Pildora("", Estilo.GRIS_FONDO, Estilo.GRIS_TEXTO,
            Estilo.fuente(Font.BOLD, 14f), -1);
    private final JLabel lblSoloAdmin = Formulario.nota(new JLabel(
            "Solo un administrador puede modificar a este empleado."));
    private final JPanel cuerpoDetalle = Estilo.transparente(new BorderLayout());

    private final JButton btnModificar = new Boton("Modificar datos", new Icono(Icono.Tipo.LAPIZ, 18, Color.WHITE),
            Boton.Tipo.PRIMARIO);
    private final JButton btnCambiarSector = new Boton("Cambiar", Boton.Tipo.CONTORNO);
    private final JButton btnBaja = new Boton("Dar de baja", Boton.Tipo.CONTORNO_CAFE);

    private final EmpleadosControlador controlador;

    public EmpleadosPanel() {
        super(new BorderLayout(0, 22));
        setBackground(Estilo.FONDO);
        setBorder(Estilo.relleno(30, 48, 28, 48));

        JPanel arriba = Estilo.transparente(new BorderLayout(0, 22));
        arriba.add(armarEncabezado(), BorderLayout.NORTH);
        arriba.add(armarFiltros(), BorderLayout.CENTER);

        add(arriba, BorderLayout.NORTH);
        add(armarCentro(), BorderLayout.CENTER);
        add(armarResumen(), BorderLayout.SOUTH);

        controlador = new EmpleadosControlador(this);

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { aplicarFiltro(); }
            @Override public void removeUpdate(DocumentEvent e) { aplicarFiltro(); }
            @Override public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
        });
        selSector.alElegir(i -> {
            sectorFiltro = sectoresDelSelector.get(i);
            limpiarMasSectores();
            aplicarFiltro();
        });
        cmbMasSectores.addActionListener(e -> {
            if (armandoFiltro || cmbMasSectores.getSelectedIndex() <= 0) {
                return;
            }
            sectorFiltro = (String) cmbMasSectores.getSelectedItem();
            selSector.limpiar();
            aplicarFiltro();
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle(empleadoSeleccionado().orElse(null));
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

        mostrarDetalle(null);
        habilitarAcciones(false);
        controlador.cargar(null);
    }

    // ---------- armado ----------

    private JComponent armarEncabezado() {
        JPanel derecha = Estilo.transparente(new GridBagLayout());
        derecha.add(btnNuevo);
        JPanel encabezado = Estilo.transparente(new BorderLayout());
        encabezado.add(Estilo.tituloPantalla("Recursos humanos", "Empleados"), BorderLayout.CENTER);
        encabezado.add(derecha, BorderLayout.EAST);
        return encabezado;
    }

    /** Buscador, sectores (los más numerosos a la vista, el resto en un desplegable) y actualizar. */
    private JComponent armarFiltros() {
        Formulario.estilizar(txtBuscar, "Buscar por nombre, DNI o usuario");
        txtBuscar.putClientProperty("JTextField.leadingIcon", new Icono(Icono.Tipo.LUPA, 18, Estilo.TEXTO_SUAVE));
        Formulario.estilizar(cmbMasSectores);
        cmbMasSectores.setPreferredSize(new Dimension(175, cmbMasSectores.getPreferredSize().height));
        cmbMasSectores.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof String s && !MAS_SECTORES.equals(s)) {
                    setText(EmpleadoTableModel.textoSector(s) + " · " + contar(s));
                }
                return this;
            }
        });
        btnActualizar.setToolTipText("Actualizar");
        btnActualizar.setBorder(Estilo.relleno(12, 12, 12, 12));

        JPanel derecha = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.add(selSector);
        derecha.add(Box.createHorizontalStrut(12));
        derecha.add(cmbMasSectores);
        derecha.add(Box.createHorizontalStrut(12));
        derecha.add(btnActualizar);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(20, 0), 22);
        tarjeta.setBorder(Estilo.relleno(16, 18, 16, 18));
        tarjeta.add(txtBuscar, BorderLayout.CENTER);
        tarjeta.add(derecha, BorderLayout.EAST);
        return tarjeta;
    }

    private JComponent armarCentro() {
        JComponent lista = armarTabla();
        JComponent detalle = armarDetalle();
        // ancho preferido mínimo: así las dos columnas se reparten el lugar según weightx
        lista.setPreferredSize(new Dimension(100, 100));
        detalle.setPreferredSize(new Dimension(100, 100));

        JPanel centro = Estilo.transparente(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.weightx = 0.66;
        c.insets = new Insets(0, 0, 0, 14);
        centro.add(lista, c);
        c.gridx = 1;
        c.weightx = 0.34;
        c.insets = new Insets(0, 14, 0, 0);
        centro.add(detalle, c);
        return centro;
    }

    private JComponent armarTabla() {
        orden.setSortKeys(List.of(new RowSorter.SortKey(EmpleadoTableModel.COL_APELLIDO, SortOrder.ASCENDING)));
        tabla.setRowSorter(orden);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Tablas.estilizar(tabla, ALTO_FILA);

        Font normal = Estilo.fuente(Font.PLAIN, 16f);
        Tablas.renderer(tabla, EmpleadoTableModel.COL_APELLIDO, new RendererEmpleado());
        Tablas.renderer(tabla, 2, new Tablas.RendererTexto(normal, Estilo.TEXTO,
                v -> EmpleadoTableModel.dni(((Number) v).intValue())));
        Tablas.renderer(tabla, 3, new Tablas.RendererTexto(normal, Estilo.TEXTO,
                v -> v.toString().isBlank() ? "—" : v.toString()));
        Tablas.renderer(tabla, 4, new Tablas.RendererTexto(normal, Estilo.TEXTO_SUAVE, String::valueOf));
        Tablas.renderer(tabla, EmpleadoTableModel.COL_SECTOR,
                new Tablas.RendererInsignia(v -> EmpleadoTableModel.insigniaSector((String) v)));
        Tablas.anchos(tabla, 10, 205, 105, 128, 102, 195);
        // el legajo va debajo del nombre
        tabla.removeColumn(tabla.getColumnModel().getColumn(EmpleadoTableModel.COL_LEGAJO));

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(Tablas.scroll(tabla), BorderLayout.CENTER);
        return tarjeta;
    }

    private JComponent armarDetalle() {
        // arriba, sobre fondo crema: iniciales, nombre, sector y legajo
        avatarDetalle.setColores(Estilo.CAFE, Color.WHITE);
        avatarDetalle.setAlignmentX(CENTER_ALIGNMENT);
        lblNombre.setFont(Estilo.fuente(Font.BOLD, 24f));
        lblNombre.setForeground(Estilo.TEXTO);
        lblNombre.setAlignmentX(CENTER_ALIGNMENT);
        lblNombre.setBorder(Estilo.relleno(0, 0, 0, 6));
        lblSector.setBorder(Estilo.relleno(4, 12, 4, 12));
        lblLegajo.setFont(Estilo.fuente(Font.PLAIN, 15f));
        lblLegajo.setForeground(Estilo.TEXTO_SUAVE);
        JPanel filaSector = Estilo.transparente(new FlowLayout(FlowLayout.CENTER, 10, 0));
        filaSector.add(lblSector);
        filaSector.add(lblLegajo);
        filaSector.setAlignmentX(CENTER_ALIGNMENT);

        JPanel cabecera = new JPanel();
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        cabecera.setBackground(Estilo.FILA_SELECCIONADA);
        cabecera.setBorder(Estilo.relleno(26, 20, 22, 20));
        cabecera.add(avatarDetalle);
        cabecera.add(Box.createVerticalStrut(12));
        cabecera.add(lblNombre);
        cabecera.add(Box.createVerticalStrut(8));
        cabecera.add(filaSector);

        // datos del empleado, uno por fila
        lblSectorFila.setBorder(Estilo.relleno(4, 12, 4, 12));
        btnCambiarSector.setBorder(Estilo.relleno(7, 14, 7, 14));
        JPanel sector = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        sector.add(lblSectorFila);
        sector.add(btnCambiarSector);

        JPanel datos = Estilo.transparente(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        fila(datos, c, "DNI", lblDni, true);
        c.gridy = 1;
        fila(datos, c, "Celular", lblCelular, true);
        c.gridy = 2;
        fila(datos, c, "Usuario del sistema", lblUsuario, true);
        c.gridy = 3;
        fila(datos, c, "Sector", sector, false);
        c.gridy = 4;
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(12, 0, 0, 0);
        datos.add(lblSoloAdmin, c);
        datos.setBorder(Estilo.relleno(14, 26, 10, 26));
        JPanel datosArriba = Estilo.transparente(new BorderLayout());
        datosArriba.add(datos, BorderLayout.NORTH);

        JPanel botones = Estilo.transparente(new GridBagLayout());
        botones.setBorder(Estilo.relleno(14, 26, 24, 26));
        btnModificar.setBorder(Estilo.relleno(14, 20, 14, 22));
        btnModificar.setFont(Estilo.fuente(Font.BOLD, 16f));
        btnBaja.setBorder(Estilo.relleno(14, 18, 14, 18));
        GridBagConstraints b = new GridBagConstraints();
        b.fill = GridBagConstraints.BOTH;
        b.insets = new Insets(0, 0, 0, 10);
        botones.add(btnBaja, b);
        b.weightx = 1;
        b.insets = new Insets(0, 0, 0, 0);
        botones.add(btnModificar, b);

        cuerpoDetalle.add(datosArriba, BorderLayout.CENTER);
        cuerpoDetalle.add(botones, BorderLayout.SOUTH);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(cabecera, BorderLayout.NORTH);
        tarjeta.add(cuerpoDetalle, BorderLayout.CENTER);
        return tarjeta;
    }

    private static JLabel valor() {
        JLabel l = new JLabel("—");
        l.setFont(Estilo.fuente(Font.BOLD, 16f));
        l.setForeground(Estilo.TEXTO);
        l.setBorder(Estilo.relleno(0, 0, 0, 4)); // la negrita no tiene que quedar recortada
        return l;
    }

    /** "DNI ........ 25.111.001" con una línea finita abajo. */
    private static void fila(JPanel panel, GridBagConstraints c, String nombre, JComponent valor, boolean linea) {
        JLabel lbl = new JLabel(nombre);
        lbl.setFont(Estilo.fuente(Font.PLAIN, 16f));
        lbl.setForeground(Estilo.TEXTO_SUAVE);
        JPanel filaPanel = Estilo.transparente(new BorderLayout(12, 0));
        filaPanel.setBorder(BorderFactory.createCompoundBorder(
                linea ? BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE) : Estilo.relleno(0, 0, 0, 0),
                Estilo.relleno(12, 0, 12, 0)));
        filaPanel.add(lbl, BorderLayout.WEST);
        filaPanel.add(valor, BorderLayout.EAST);
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);
        panel.add(filaPanel, c);
    }

    private JComponent armarResumen() {
        lblResumen.setFont(Estilo.fuente(Font.BOLD, 16f));
        lblResumen.setForeground(Estilo.TEXTO);
        lblResumenDetalle.setFont(Estilo.fuente(Font.PLAIN, 16f));
        lblResumenDetalle.setForeground(Estilo.TEXTO_SUAVE);
        JPanel resumen = Estilo.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        resumen.add(lblResumen);
        resumen.add(lblResumenDetalle);
        return resumen;
    }

    // ---------- detalle ----------

    private void mostrarDetalle(Empleado e) {
        if (e == null) {
            avatarDetalle.setNombre("");
            lblNombre.setText("Elegí un empleado");
            lblSector.setVisible(false);
            lblLegajo.setText("Acá vas a ver sus datos y las acciones.");
            cuerpoDetalle.setVisible(false);
            return;
        }
        cuerpoDetalle.setVisible(true);
        String nombre = e.getNombre() + " " + e.getApellido();
        avatarDetalle.setNombre(nombre);
        lblNombre.setText(nombre);
        String sector = EmpleadoTableModel.sector(e);
        lblSector.setText(EmpleadoTableModel.textoSector(sector));
        lblSector.setVisible(true);
        lblLegajo.setText("Legajo " + e.getLegajo());
        lblDni.setText(EmpleadoTableModel.dni(e.getDni()));
        lblCelular.setText(e.getCelular() == null || e.getCelular().isBlank() ? "—" : e.getCelular());
        lblUsuario.setText(e.getUsername() == null ? "—" : e.getUsername());
        Tablas.Insignia insignia = EmpleadoTableModel.insigniaSector(sector);
        lblSectorFila.setText(insignia.texto());
        lblSectorFila.setFondo(insignia.fondo());
        lblSectorFila.setForeground(insignia.color());
    }

    // ---------- lo que usa el controlador ----------

    public void mostrarEmpleados(List<Empleado> empleados) {
        String elegido = sectorFiltro;
        modelo.setFilas(empleados);
        armarFiltroSectores(empleados, elegido);
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
                mostrarDetalle(empleadoSeleccionado().orElse(null)); // los datos pueden haber cambiado
                return;
            }
        }
    }

    public void habilitarAcciones(boolean habilitado) {
        btnModificar.setEnabled(habilitado);
        btnCambiarSector.setEnabled(habilitado);
        btnBaja.setEnabled(habilitado);
        // si hay alguien elegido pero no se lo puede tocar, se explica por qué
        lblSoloAdmin.setVisible(!habilitado && empleadoSeleccionado().isPresent());
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

    /**
     * "Todos · 10" y los sectores con más empleados a la vista; el resto en "Más sectores".
     * Si se estaba filtrando por un sector que sigue existiendo, queda elegido.
     */
    private void armarFiltroSectores(List<Empleado> empleados, String elegido) {
        Map<String, Long> porSector = empleados.stream()
                .map(EmpleadoTableModel::sector)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()));
        List<String> ordenados = porSector.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey)
                .toList();
        List<String> aLaVista = ordenados.stream().limit(SECTORES_A_LA_VISTA).toList();
        List<String> resto = ordenados.stream().skip(SECTORES_A_LA_VISTA)
                .sorted(Comparator.comparing(EmpleadoTableModel::textoSector)).toList();

        armandoFiltro = true;
        try {
            sectoresDelSelector.clear();
            sectoresDelSelector.add(null);
            List<String> textos = new ArrayList<>();
            textos.add("Todos · " + empleados.size());
            for (String s : aLaVista) {
                sectoresDelSelector.add(s);
                textos.add(EmpleadoTableModel.textoSector(s) + " · " + porSector.get(s));
            }
            selSector.setOpciones(textos.toArray(new String[0]));

            DefaultComboBoxModel<String> masModelo = new DefaultComboBoxModel<>();
            masModelo.addElement(MAS_SECTORES);
            resto.forEach(masModelo::addElement);
            cmbMasSectores.setModel(masModelo);
            cmbMasSectores.setVisible(!resto.isEmpty());

            // se mantiene el filtro que estaba elegido
            sectorFiltro = null;
            if (elegido != null && aLaVista.contains(elegido)) {
                selSector.elegir(sectoresDelSelector.indexOf(elegido));
                sectorFiltro = elegido;
            } else if (elegido != null && resto.contains(elegido)) {
                selSector.limpiar();
                cmbMasSectores.setSelectedItem(elegido);
                sectorFiltro = elegido;
            }
        } finally {
            armandoFiltro = false;
        }
        lblResumen.setText(empleados.size() + (empleados.size() == 1 ? " empleado" : " empleados"));
        lblResumenDetalle.setText(" · " + porSector.size() + (porSector.size() == 1 ? " sector" : " sectores"));
    }

    private void limpiarMasSectores() {
        armandoFiltro = true;
        cmbMasSectores.setSelectedIndex(cmbMasSectores.getItemCount() > 0 ? 0 : -1);
        armandoFiltro = false;
    }

    private long contar(String sector) {
        long n = 0;
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (sector.equals(EmpleadoTableModel.sector(modelo.getFila(i)))) {
                n++;
            }
        }
        return n;
    }

    private void aplicarFiltro() {
        String texto = txtBuscar.getText().trim().toLowerCase();
        String sector = sectorFiltro;
        orden.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends EmpleadoTableModel, ? extends Integer> fila) {
                Empleado e = fila.getModel().getFila(fila.getIdentifier());
                boolean coincideSector = sector == null || sector.equals(EmpleadoTableModel.sector(e));
                boolean coincideTexto = texto.isEmpty()
                        || (e.getApellido() + " " + e.getNombre()).toLowerCase().contains(texto)
                        || (e.getNombre() + " " + e.getApellido()).toLowerCase().contains(texto)
                        || (e.getUsername() != null && e.getUsername().toLowerCase().contains(texto))
                        || String.valueOf(e.getDni()).contains(texto.replace(".", ""));
                return coincideSector && coincideTexto;
            }
        });
    }

    // ---------- celdas ----------

    /** Iniciales en un círculo, "Apellido, Nombre" en negrita y debajo el legajo. */
    private class RendererEmpleado implements TableCellRenderer {
        private final JPanel celda = new JPanel(new GridBagLayout());
        private final Avatar avatar = new Avatar(40, 14f);
        private final JLabel nombre = new JLabel();
        private final JLabel legajo = new JLabel();

        RendererEmpleado() {
            nombre.setFont(Estilo.fuente(Font.BOLD, 16f));
            nombre.setForeground(Estilo.TEXTO);
            legajo.setFont(Estilo.fuente(Font.PLAIN, 14f));
            legajo.setForeground(Estilo.TEXTO_SUAVE);
            JPanel textos = Estilo.transparente(new GridLayout(2, 1, 0, 0));
            textos.add(nombre);
            textos.add(legajo);
            // alto fijo de dos renglones: acá los textos todavía están vacíos
            textos.setPreferredSize(new Dimension(140, 42));

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(0, 0, 0, 12);
            celda.add(avatar, c);
            c.insets = new Insets(0, 0, 0, 0);
            celda.add(textos, c);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            Empleado e = modelo.getFila(t.convertRowIndexToModel(fila));
            nombre.setText(e.getApellido() + ", " + e.getNombre());
            legajo.setText("Legajo " + e.getLegajo());
            avatar.setNombre(e.getNombre() + " " + e.getApellido());
            avatar.setColores(seleccionada ? Estilo.CAFE : Estilo.FILA_SELECCIONADA,
                    seleccionada ? Color.WHITE : Estilo.CAFE);
            celda.setBackground(seleccionada ? t.getSelectionBackground() : t.getBackground());
            // la rayita café de la fila elegida, igual que en las demás tablas
            celda.setBorder(seleccionada && columna == 0
                    ? BorderFactory.createMatteBorder(0, 4, 0, 0, Estilo.CAFE) : Estilo.relleno(0, 4, 0, 0));
            return celda;
        }
    }
}
