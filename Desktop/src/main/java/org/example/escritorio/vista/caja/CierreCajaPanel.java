package org.example.escritorio.vista.caja;

import org.example.Modelo.facturacion.CierreCaja;
import org.example.escritorio.controlador.caja.CierreCajaControlador;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formulario;
import org.example.escritorio.vista.comun.Icono;
import org.example.escritorio.vista.comun.Tablas;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Cierre de caja del día (RF-47).
 * A la izquierda se carga el efectivo contado; a la derecha el resumen del día: el efectivo que tendría que
 * haber en la caja (lo cobrado hoy en efectivo), la diferencia con lo contado mientras se escribe,
 * y lo cobrado con tarjeta y QR. Abajo, los cierres anteriores: se filtran por mes y con doble clic
 * (o "Ver detalle") se abre el cierre como papel, con los cobros de ese día.
 */
public class CierreCajaPanel extends JPanel {

    private static final Locale ARGENTINA = Locale.forLanguageTag("es-AR");
    private static final DateTimeFormatter DIA_LARGO = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", ARGENTINA);
    private static final DateTimeFormatter DIA_CORTO = DateTimeFormatter.ofPattern("dd/MM");
    private static final int ALTO_FILA = 56;

    private final JSpinner spnContado = new JSpinner(new SpinnerNumberModel(0d, 0d, 100_000_000d, 1000d));
    private final JTextField txtObservaciones = new JTextField(30);
    private final JButton btnCerrar = new Boton("Cerrar caja", new Icono(Icono.Tipo.CANDADO, 18, Color.WHITE),
            Boton.Tipo.PRIMARIO);

    // resumen del día
    private final JLabel lblEsperado = valor();
    private final JLabel lblContado = valor();
    private final Estilo.Pildora lblDiferencia = new Estilo.Pildora("", Estilo.GRIS_FONDO, Estilo.GRIS_TEXTO,
            Estilo.fuente(Font.BOLD, 14f), -1);
    private final JLabel lblArqueoPendiente = Formulario.nota(new JLabel("No cuadra: anotá el motivo en Observaciones."));
    private final JPanel filasArqueo = Estilo.transparente(new GridBagLayout());
    private final JLabel lblTarjeta = valor();
    private final JLabel lblQr = valor();
    private final JLabel lblTotalDia = new JLabel("—");
    /** Lo cobrado hoy en efectivo (lo que tendría que haber en la caja); null hasta que carga. */
    private BigDecimal esperado;

    private final CierreTableModel modelo = new CierreTableModel();
    private final JTable tabla = new JTable(modelo) {
        @Override
        public String getToolTipText(java.awt.event.MouseEvent e) {
            int fila = rowAtPoint(e.getPoint());
            if (fila < 0) {
                return null;
            }
            String obs = modelo.getFila(convertRowIndexToModel(fila)).cierre().getObservaciones();
            return obs == null || obs.isBlank() ? null : "Observaciones: " + obs;
        }
    };
    private final JButton btnActualizar = new Boton(null, new Icono(Icono.Tipo.ACTUALIZAR, 20, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);
    private final JButton btnDetalle = new Boton("Ver detalle", new Icono(Icono.Tipo.DOCUMENTO, 18, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);
    /** null = todos los meses. */
    private final javax.swing.JComboBox<java.time.YearMonth> cmbMes = new javax.swing.JComboBox<>();
    private final TableRowSorter<CierreTableModel> orden = new TableRowSorter<>(modelo);
    /** true mientras se rearma el combo de meses: no hay que filtrar por esos cambios. */
    private boolean armandoMeses;

    private final CierreCajaControlador controlador;
    /** Cada cuánto se vuelve a pedir lo cobrado (si en Cobros se cobra algo, aparece solo). */
    private static final int REFRESCO_MS = 10_000;
    private final javax.swing.Timer refresco;

    public CierreCajaPanel() {
        super(new BorderLayout(0, 26));
        setOpaque(false);
        setBorder(Estilo.relleno(4, 0, 0, 0));

        JPanel arriba = Estilo.transparente(new GridLayout(1, 2, 28, 0));
        arriba.add(armarFormulario());
        arriba.add(armarResumen());

        add(arriba, BorderLayout.NORTH);
        add(armarHistorial(), BorderLayout.CENTER);

        controlador = new CierreCajaControlador(this);
        btnCerrar.addActionListener(e -> controlador.cerrarCaja());
        btnActualizar.addActionListener(e -> controlador.cargar());
        btnDetalle.addActionListener(e -> cierreSeleccionado().ifPresent(controlador::verDetalle));

        // solo refresca si la solapa está a la vista; el timer se apaga al cerrar la ventana
        refresco = new javax.swing.Timer(REFRESCO_MS, e -> {
            if (isShowing()) {
                controlador.cargar(false);
            }
        });

        controlador.cargar();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        refresco.start();
    }

    @Override
    public void removeNotify() {
        refresco.stop();
        super.removeNotify();
    }

    // ---------- armado ----------

    /** Tarjeta de la izquierda: el efectivo contado, observaciones y el botón de cerrar. */
    private JComponent armarFormulario() {
        String dia = DIA_LARGO.format(LocalDate.now()).toUpperCase(ARGENTINA);
        JLabel lblDia = new JLabel(dia);
        lblDia.setFont(Estilo.conEspaciado(Estilo.fuente(Font.BOLD, 13f), 0.1f));
        lblDia.setForeground(Estilo.CAFE);
        lblDia.setBorder(Estilo.relleno(0, 0, 0, 8));
        JLabel titulo = new JLabel("Cierre de caja de hoy");
        titulo.setFont(Estilo.fuente(Font.BOLD, 26f));
        titulo.setForeground(Estilo.TEXTO);
        JLabel ayuda = Formulario.nota(new JLabel("Contá el efectivo y cargalo: a la derecha ves si cuadra con lo cobrado."));

        // el efectivo contado va grande, con el signo $ adelante
        spnContado.setEditor(new JSpinner.NumberEditor(spnContado, "#,##0.##"));
        spnContado.setFont(Estilo.fuente(Font.BOLD, 28f));
        Estilo.flat(spnContado, "arc: 16; padding: 12,14,12,14; buttonStyle: none;"
                + " " + Estilo.bordeCampo(3));
        // sin flechitas: el efectivo se escribe
        for (java.awt.Component hijo : spnContado.getComponents()) {
            if (hijo instanceof JButton) {
                hijo.setVisible(false);
            }
        }
        // la diferencia se actualiza mientras se escribe, no recién al salir del campo
        spnContado.addChangeListener(e -> actualizarArqueo());
        ((JSpinner.DefaultEditor) spnContado.getEditor()).getTextField().getDocument()
                .addDocumentListener(new javax.swing.event.DocumentListener() {
                    @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarArqueo(); }
                    @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarArqueo(); }
                    @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarArqueo(); }
                });
        JLabel signo = new JLabel("$");
        signo.setFont(Estilo.fuente(Font.BOLD, 26f));
        signo.setForeground(Estilo.CAFE);
        JPanel contado = Estilo.transparente(new BorderLayout(12, 0));
        contado.add(signo, BorderLayout.WEST);
        contado.add(spnContado, BorderLayout.CENTER);

        Formulario.estilizar(txtObservaciones, "Ej.: faltante por vuelto mal dado, retiro para proveedor...");
        btnCerrar.setBorder(Estilo.relleno(15, 22, 15, 22));
        btnCerrar.setFont(Estilo.fuente(Font.BOLD, 17f));

        JPanel textos = Estilo.transparente(null);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(lblDia);
        textos.add(Box.createVerticalStrut(6));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(ayuda);

        JPanel cuerpo = Formulario.cuerpo(
                textos,
                Formulario.campo("Efectivo contado", contado, null),
                Formulario.campo("Observaciones", txtObservaciones, null),
                btnCerrar);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(26, 30, 28, 30));
        tarjeta.add(cuerpo, BorderLayout.NORTH);
        return tarjeta;
    }

    /** Tarjeta de la derecha: arqueo del efectivo (esperado, contado y diferencia), tarjeta, QR y total del día. */
    private JComponent armarResumen() {
        JLabel titulo = new JLabel("Resumen del día");
        titulo.setFont(Estilo.fuente(Font.BOLD, 20f));
        titulo.setForeground(Estilo.TEXTO);

        lblDiferencia.setBorder(Estilo.relleno(5, 14, 5, 14));
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        fila(filasArqueo, c, "Efectivo cobrado hoy", lblEsperado);
        c.gridy = 1;
        fila(filasArqueo, c, "Efectivo contado", lblContado);
        c.gridy = 2;
        fila(filasArqueo, c, "Diferencia", lblDiferencia);

        Estilo.Tarjeta arqueo = new Estilo.Tarjeta(new BorderLayout(0, 6), 16);
        arqueo.setBackground(Estilo.FILA_SELECCIONADA);
        arqueo.setBorder(Estilo.relleno(16, 22, 16, 22));
        arqueo.add(filasArqueo, BorderLayout.CENTER);
        arqueo.add(lblArqueoPendiente, BorderLayout.SOUTH);

        JPanel otros = Estilo.transparente(new GridBagLayout());
        otros.setBorder(Estilo.relleno(4, 6, 0, 6));
        c = new GridBagConstraints();
        c.gridy = 0;
        fila(otros, c, "Tarjeta", lblTarjeta);
        c.gridy = 1;
        fila(otros, c, "QR", lblQr);

        JLabel total = new JLabel("Total del día");
        total.setFont(Estilo.fuente(Font.BOLD, 18f));
        total.setForeground(Estilo.TEXTO);
        lblTotalDia.setFont(Estilo.fuente(Font.BOLD, 30f));
        lblTotalDia.setForeground(Estilo.CAFE_OSCURO);
        lblTotalDia.setBorder(Estilo.relleno(0, 0, 0, 6));
        JPanel filaTotal = Estilo.transparente(new BorderLayout());
        filaTotal.setBorder(BorderFactory.createCompoundBorder(new Estilo.LineaPunteada(true), Estilo.relleno(14, 0, 0, 0)));
        filaTotal.add(total, BorderLayout.WEST);
        filaTotal.add(lblTotalDia, BorderLayout.EAST);

        JPanel cuerpo = Formulario.cuerpo(titulo, arqueo, otros, filaTotal);
        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(26, 30, 28, 30));
        tarjeta.add(cuerpo, BorderLayout.NORTH);
        return tarjeta;
    }

    private static JLabel valor() {
        JLabel l = new JLabel("—");
        l.setFont(Estilo.fuente(Font.BOLD, 16f));
        l.setForeground(Estilo.TEXTO);
        return l;
    }

    private static void fila(JPanel panel, GridBagConstraints c, String nombre, JComponent valor) {
        JLabel lbl = new JLabel(nombre);
        lbl.setFont(Estilo.fuente(Font.PLAIN, 16f));
        lbl.setForeground(Estilo.TEXTO_SUAVE);
        c.gridx = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 0, 5, 12);
        panel.add(lbl, c);
        c.gridx = 1;
        c.weightx = 0;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(5, 12, 5, 0);
        panel.add(valor, c);
    }

    private JComponent armarHistorial() {
        orden.setSortKeys(List.of(new RowSorter.SortKey(CierreTableModel.COL_NRO, SortOrder.DESCENDING)));
        tabla.setRowSorter(orden);
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> btnDetalle.setEnabled(tabla.getSelectedRow() >= 0));
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.rowAtPoint(e.getPoint()) >= 0) {
                    cierreSeleccionado().ifPresent(controlador::verDetalle);
                }
            }
        });
        Tablas.estilizar(tabla, ALTO_FILA);

        Font normal = Estilo.fuente(Font.PLAIN, 16f);
        Font negrita = Estilo.fuente(Font.BOLD, 16f);
        Tablas.renderer(tabla, 0, new Tablas.RendererTexto(normal, Estilo.TEXTO_SUAVE, v -> "#" + v));
        Tablas.renderer(tabla, 1, new Tablas.RendererTexto(normal, Estilo.TEXTO, v -> DIA_CORTO.format((LocalDate) v)));
        Tablas.renderer(tabla, 2, new Tablas.RendererTexto(normal, Estilo.TEXTO,
                v -> v.toString().isBlank() ? "—" : v.toString()));
        for (int col : new int[]{3, 4, 6, 7}) {
            Tablas.renderer(tabla, col, new Tablas.RendererTexto(normal, Estilo.TEXTO,
                    v -> Tablas.pesosCortos((BigDecimal) v)));
        }
        Tablas.renderer(tabla, CierreTableModel.COL_DIFERENCIA, new Tablas.RendererInsignia(v -> diferencia((BigDecimal) v)));
        Tablas.renderer(tabla, CierreTableModel.COL_TOTAL, new Tablas.RendererTexto(negrita, Estilo.TEXTO,
                v -> Tablas.pesosCortos((BigDecimal) v)));
        Tablas.renderer(tabla, 9, new Tablas.RendererTexto(normal, Estilo.TEXTO_SUAVE,
                v -> v.toString().isBlank() ? "—" : v.toString()));
        Tablas.anchos(tabla, 65, 75, 140, 110, 110, 160, 110, 110, 160, 10);
        // las observaciones no entran en la fila: se ven al pasar el mouse
        tabla.removeColumn(tabla.getColumnModel().getColumn(9));

        JLabel titulo = new JLabel("Cierres anteriores");
        titulo.setFont(Estilo.fuente(Font.BOLD, 22f));
        titulo.setForeground(Estilo.TEXTO);
        btnActualizar.setToolTipText("Actualizar");
        btnActualizar.setBorder(Estilo.relleno(12, 12, 12, 12));
        btnDetalle.setBorder(Estilo.relleno(11, 16, 11, 18));
        btnDetalle.setEnabled(false);
        Formulario.estilizar(cmbMes);
        cmbMes.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> lista, Object valor, int indice,
                                                                   boolean elegido, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, elegido, foco);
                setText(valor == null ? "Todos los meses" : nombreMes((java.time.YearMonth) valor));
                return this;
            }
        });
        cmbMes.addItem(null);
        cmbMes.addActionListener(e -> {
            if (!armandoMeses) {
                filtrarPorMes();
            }
        });
        JPanel acciones = Estilo.transparente(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 0));
        acciones.add(cmbMes);
        acciones.add(btnDetalle);
        acciones.add(btnActualizar);
        JPanel encabezado = Estilo.transparente(new BorderLayout());
        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(acciones, BorderLayout.EAST);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(Tablas.scroll(tabla), BorderLayout.CENTER);
        // la tarjeta mide lo que ocupan las filas (como mucho, el alto disponible)
        JPanel contenedor = Estilo.transparente(new Tablas.Apilado(0, () -> Tablas.altoContenido(tabla)));
        contenedor.add(tarjeta);

        JPanel historial = Estilo.transparente(new BorderLayout(0, 14));
        historial.add(encabezado, BorderLayout.NORTH);
        historial.add(contenedor, BorderLayout.CENTER);
        return historial;
    }

    /** "Cuadra" en verde, "Sobran $ 600" en amarillo, "Faltan $ 2.000" en rojo. */
    private static Tablas.Insignia diferencia(BigDecimal d) {
        if (d == null || d.signum() == 0) {
            return Tablas.Insignia.verde("Cuadra");
        }
        if (d.signum() > 0) {
            return Tablas.Insignia.amarilla("Sobran " + Tablas.pesosCortos(d));
        }
        return Tablas.Insignia.roja("Faltan " + Tablas.pesosCortos(d.negate()));
    }

    // ---------- lo que usa el controlador ----------

    /** El monto contado con 2 decimales; null si lo escrito no es un número. */
    public BigDecimal getMontoContado() {
        try {
            spnContado.commitEdit();
        } catch (ParseException e) {
            return null;
        }
        return BigDecimal.valueOf(((Number) spnContado.getValue()).doubleValue()).setScale(2, RoundingMode.HALF_UP);
    }

    public String getObservaciones() { return txtObservaciones.getText().trim(); }

    public JButton getBotonCerrar() { return btnCerrar; }
    public JButton getBotonDetalle() { return btnDetalle; }
    public JButton getBotonActualizar() { return btnActualizar; }

    /**
     * Muestra los cierres. Como se vuelve a llamar con el refresco automático, se mantienen el mes
     * y el cierre elegidos.
     */
    public void mostrarCierres(List<CierreFila> cierres) {
        Integer elegido = cierreSeleccionado().map(f -> f.cierre().getIdCierre()).orElse(null);
        modelo.setFilas(cierres);
        armarMeses(cierres);
        filtrarPorMes();
        if (elegido != null) {
            for (int i = 0; i < tabla.getRowCount(); i++) {
                if (modelo.getFila(tabla.convertRowIndexToModel(i)).cierre().getIdCierre() == elegido) {
                    tabla.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
        revalidate();
        repaint();
    }

    /** El cierre elegido en la tabla de cierres anteriores. */
    public java.util.Optional<CierreFila> cierreSeleccionado() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? java.util.Optional.empty()
                : java.util.Optional.of(modelo.getFila(tabla.convertRowIndexToModel(fila)));
    }

    /** "Todos los meses" y los meses que tienen cierres, del más nuevo al más viejo; queda el que estaba elegido. */
    private void armarMeses(List<CierreFila> cierres) {
        java.time.YearMonth elegido = (java.time.YearMonth) cmbMes.getSelectedItem();
        List<java.time.YearMonth> meses = cierres.stream()
                .map(f -> java.time.YearMonth.from(f.cierre().getFecha()))
                .distinct()
                .sorted(java.util.Comparator.reverseOrder())
                .toList();
        armandoMeses = true;
        cmbMes.removeAllItems();
        cmbMes.addItem(null);
        meses.forEach(cmbMes::addItem);
        cmbMes.setSelectedItem(elegido != null && meses.contains(elegido) ? elegido : null);
        armandoMeses = false;
    }

    private void filtrarPorMes() {
        java.time.YearMonth mes = (java.time.YearMonth) cmbMes.getSelectedItem();
        orden.setRowFilter(mes == null ? null : new javax.swing.RowFilter<CierreTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends CierreTableModel, ? extends Integer> fila) {
                return java.time.YearMonth.from(modelo.getFila(fila.getIdentifier()).cierre().getFecha()).equals(mes);
            }
        });
    }

    /** "Octubre 2026". */
    private static String nombreMes(java.time.YearMonth mes) {
        String nombre = mes.getMonth().getDisplayName(java.time.format.TextStyle.FULL, ARGENTINA);
        return Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1) + " " + mes.getYear();
    }

    /** Lo cobrado hoy en salón con cada método: el efectivo es lo que tendría que haber en la caja. */
    public void mostrarCobradoHoy(BigDecimal efectivo, BigDecimal tarjeta, BigDecimal qr) {
        esperado = efectivo;
        lblEsperado.setText(Tablas.pesosCortos(efectivo));
        lblTarjeta.setText(Tablas.pesosCortos(tarjeta));
        lblQr.setText(Tablas.pesosCortos(qr));
        lblTotalDia.setText(Tablas.pesosCortos(efectivo.add(tarjeta).add(qr)));
        actualizarArqueo();
    }

    /** Lo cobrado hoy en efectivo, como se muestra (null si todavía no cargó). */
    public BigDecimal getEfectivoEsperado() {
        return esperado;
    }

    /** Contado y diferencia con lo que se está escribiendo; si todavía no es un número, queda "—". */
    private void actualizarArqueo() {
        BigDecimal contado = contadoEscrito();
        lblContado.setText(contado == null ? "—" : Tablas.pesosCortos(contado));
        if (contado == null || esperado == null) {
            pintarDiferencia(new Tablas.Insignia("—", Estilo.GRIS_FONDO, Estilo.GRIS_TEXTO), false);
        } else {
            BigDecimal d = contado.subtract(esperado);
            pintarDiferencia(diferencia(d), d.signum() != 0);
        }
    }

    /** Lee lo escrito en el campo sin confirmarlo (para no mover el cursor mientras se escribe). */
    private BigDecimal contadoEscrito() {
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor) spnContado.getEditor();
        String texto = editor.getTextField().getText().trim();
        if (texto.isEmpty()) {
            return null;
        }
        java.text.ParsePosition pos = new java.text.ParsePosition(0);
        Number n = editor.getFormat().parse(texto, pos);
        if (n == null || pos.getIndex() != texto.length() || n.doubleValue() < 0) {
            return null;
        }
        return BigDecimal.valueOf(n.doubleValue()).setScale(2, RoundingMode.HALF_UP);
    }

    private void pintarDiferencia(Tablas.Insignia d, boolean noCuadra) {
        lblDiferencia.setText(d.texto());
        lblDiferencia.setFondo(d.fondo());
        lblDiferencia.setForeground(d.color());
        lblArqueoPendiente.setVisible(noCuadra);
    }

    /**
     * Muestra el cierre recién hecho con lo que guardó el backend y limpia las observaciones.
     * El contado queda escrito, así se sigue viendo el arqueo de ese cierre.
     */
    public void mostrarResultado(CierreFila fila) {
        CierreCaja cierre = fila.cierre();
        esperado = cierre.getMontoEsperado();
        lblEsperado.setText(Tablas.pesosCortos(esperado));
        spnContado.setValue(cierre.getMontoContado().doubleValue());
        actualizarArqueo();
        lblTarjeta.setText(Tablas.pesosCortos(fila.tarjeta()));
        lblQr.setText(Tablas.pesosCortos(fila.qr()));
        lblTotalDia.setText(Tablas.pesosCortos(fila.totalDelDia()));
        txtObservaciones.setText("");
    }

    public void mostrarError(String mensaje) {
        Dialogos.error(this, mensaje);
    }

    public boolean confirmar(String pregunta) {
        return Dialogos.confirmar(this, pregunta);
    }
}
