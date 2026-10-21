package org.example.escritorio.vista.caja;

import org.example.Modelo.enums.MetodoPago;
import org.example.escritorio.controlador.caja.ReporteDeliveryControlador;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.comun.Formulario;
import org.example.escritorio.vista.comun.Icono;
import org.example.escritorio.vista.comun.ModeloTablaBase;
import org.example.escritorio.vista.comun.Selector;
import org.example.escritorio.vista.comun.Tablas;
import org.example.escritorio.vista.comun.TarjetaDato;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Reporte de delivery de un día (RF-48): lo cobrado en envíos, separado por método de pago,
 * y la lista de pedidos cobrados. Solo cuenta facturaciones pagadas; los totales los calcula FacturacionService.
 */
public class ReporteDeliveryPanel extends JPanel {

    private static final Locale ARGENTINA = Locale.forLanguageTag("es-AR");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy", ARGENTINA);
    private static final int ALTO_FILA = 54;

    private LocalDate fecha = LocalDate.now();

    private final JButton btnAnterior = new Boton(null, new Icono(Icono.Tipo.IZQUIERDA, 18, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);
    private final JButton btnSiguiente = new Boton(null, new Icono(Icono.Tipo.DERECHA, 18, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);
    private final JLabel lblFecha = new JLabel(" ", new Icono(Icono.Tipo.CALENDARIO, 20, Estilo.CAFE), JLabel.LEFT);
    private final Selector selDia = new Selector("Hoy", "Ayer");
    private final JButton btnActualizar = new Boton(null, new Icono(Icono.Tipo.ACTUALIZAR, 20, Estilo.TEXTO),
            Boton.Tipo.CONTORNO);

    private final TarjetaDato datoTotal = new TarjetaDato("Total delivery", true);
    private final TarjetaDato datoPedidos = new TarjetaDato("Pedidos cobrados", false);
    private final TarjetaDato datoPromedio = new TarjetaDato("Ticket promedio", false);
    private final TarjetaDato datoEfectivo = new TarjetaDato("Efectivo a rendir", false);

    private final JPanel panelMetodos = Estilo.transparente(new GridBagLayout());
    private final JLabel lblCantidad = Formulario.nota(new JLabel(" "));

    private final ModeloTablaBase<DeliveryFila> modelo = new ModeloTablaBase<>(
            "Ticket", "Hora", "Cliente", "Repartidor", "Método", "Total") {
        @Override
        protected Object valor(DeliveryFila d, int columna) {
            return switch (columna) {
                case 0 -> d.nroTicket();
                case 1 -> d.hora();
                case 2 -> d.cliente();
                case 3 -> d.repartidor();
                case 4 -> d.metodo() == null ? null : CobrosPanel.texto(d.metodo());
                case 5 -> d.total();
                default -> null;
            };
        }

        @Override
        protected Class<?> tipo(int columna) {
            return switch (columna) {
                case 0 -> Integer.class;
                case 1 -> LocalTime.class;
                case 5 -> BigDecimal.class;
                default -> String.class;
            };
        }
    };
    private final JTable tabla = new JTable(modelo);

    private final ReporteDeliveryControlador controlador;

    public ReporteDeliveryPanel() {
        super(new BorderLayout(0, 22));
        setOpaque(false);
        setBorder(Estilo.relleno(4, 0, 0, 0));

        JPanel arriba = Estilo.transparente(new BorderLayout(0, 22));
        arriba.add(armarFecha(), BorderLayout.NORTH);
        arriba.add(armarDatos(), BorderLayout.CENTER);

        add(arriba, BorderLayout.NORTH);
        add(armarCentro(), BorderLayout.CENTER);

        controlador = new ReporteDeliveryControlador(this);
        btnAnterior.addActionListener(e -> cambiarFecha(fecha.minusDays(1)));
        btnSiguiente.addActionListener(e -> cambiarFecha(fecha.plusDays(1)));
        selDia.alElegir(i -> cambiarFecha(LocalDate.now().minusDays(i)));
        btnActualizar.addActionListener(e -> controlador.verReporte());

        cambiarFecha(LocalDate.now());
    }

    // ---------- armado ----------

    /** ‹ [Martes 20/10/2026] ›  Hoy | Ayer  ...  actualizar */
    private JComponent armarFecha() {
        for (JButton b : new JButton[]{btnAnterior, btnSiguiente, btnActualizar}) {
            b.setBorder(Estilo.relleno(12, 12, 12, 12));
        }
        btnAnterior.setToolTipText("Día anterior");
        btnSiguiente.setToolTipText("Día siguiente");
        btnActualizar.setToolTipText("Actualizar");

        lblFecha.setFont(Estilo.fuente(Font.BOLD, 17f));
        lblFecha.setForeground(Estilo.TEXTO);
        lblFecha.setIconTextGap(12);
        Estilo.Tarjeta cajaFecha = new Estilo.Tarjeta(new BorderLayout(), 14);
        cajaFecha.setBorder(Estilo.relleno(10, 20, 10, 24));
        cajaFecha.add(lblFecha, BorderLayout.CENTER);
        cajaFecha.setPreferredSize(new Dimension(300, cajaFecha.getPreferredSize().height));

        JPanel izquierda = Estilo.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izquierda.add(btnAnterior);
        izquierda.add(Box.createHorizontalStrut(12));
        izquierda.add(cajaFecha);
        izquierda.add(Box.createHorizontalStrut(12));
        izquierda.add(btnSiguiente);
        izquierda.add(Box.createHorizontalStrut(16));
        izquierda.add(selDia);

        JPanel fila = Estilo.transparente(new BorderLayout());
        fila.add(izquierda, BorderLayout.WEST);
        fila.add(btnActualizar, BorderLayout.EAST);
        return fila;
    }

    private JComponent armarDatos() {
        datoEfectivo.setColorValor(Estilo.AMARILLO_TEXTO);
        JPanel datos = Estilo.transparente(new GridLayout(1, 4, 20, 0));
        datos.add(datoTotal);
        datos.add(datoPedidos);
        datos.add(datoPromedio);
        datos.add(datoEfectivo);
        return datos;
    }

    private JComponent armarCentro() {
        JComponent metodos = armarMetodos();
        JComponent pedidos = armarPedidos();
        metodos.setPreferredSize(new Dimension(100, 100));
        pedidos.setPreferredSize(new Dimension(100, 100));

        JPanel centro = Estilo.transparente(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        c.weighty = 1;
        c.weightx = 0.42;
        c.insets = new Insets(0, 0, 0, 14);
        centro.add(arriba(metodos), c);
        c.gridx = 1;
        c.weightx = 0.58;
        c.insets = new Insets(0, 14, 0, 0);
        centro.add(arriba(pedidos), c);
        return centro;
    }

    /** Deja la tarjeta arriba, con el alto que pide su contenido. */
    private static JComponent arriba(JComponent tarjeta) {
        JPanel p = Estilo.transparente(new Tablas.Apilado(0, () -> tarjeta.getMinimumSize().height));
        p.add(tarjeta);
        return p;
    }

    private JComponent armarMetodos() {
        JLabel titulo = new JLabel("Por método de pago");
        titulo.setFont(Estilo.fuente(Font.BOLD, 20f));
        titulo.setForeground(Estilo.TEXTO);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(0, 18), 22) {
            @Override
            public Dimension getMinimumSize() {
                // el alto que pide el contenido (el preferido está fijo para repartir el ancho)
                return new Dimension(100, getLayout().preferredLayoutSize(this).height);
            }
        };
        tarjeta.setBorder(Estilo.relleno(24, 28, 26, 28));
        tarjeta.add(titulo, BorderLayout.NORTH);
        tarjeta.add(panelMetodos, BorderLayout.CENTER);
        return tarjeta;
    }

    private JComponent armarPedidos() {
        Tablas.estilizar(tabla, ALTO_FILA);
        tabla.setRowSelectionAllowed(false);
        Font normal = Estilo.fuente(Font.PLAIN, 16f);
        Font negrita = Estilo.fuente(Font.BOLD, 16f);
        Tablas.renderer(tabla, 0, new Tablas.RendererTexto(normal, Estilo.TEXTO_SUAVE, v -> "#T-" + v));
        Tablas.renderer(tabla, 1, new Tablas.RendererTexto(normal, Estilo.TEXTO, v -> Formatos.hora((LocalTime) v)));
        Tablas.renderer(tabla, 2, new Tablas.RendererTexto(normal, Estilo.TEXTO, String::valueOf));
        Tablas.renderer(tabla, 3, new Tablas.RendererTexto(normal, Estilo.TEXTO, String::valueOf));
        Tablas.renderer(tabla, 4, new Tablas.RendererTexto(normal, Estilo.TEXTO, String::valueOf));
        Tablas.renderer(tabla, 5, new Tablas.RendererTexto(negrita, Estilo.TEXTO,
                v -> Tablas.pesosCortos((BigDecimal) v)));
        Tablas.anchos(tabla, 85, 80, 140, 130, 100, 115);

        JLabel titulo = new JLabel("Pedidos del día");
        titulo.setFont(Estilo.fuente(Font.BOLD, 20f));
        titulo.setForeground(Estilo.TEXTO);
        JPanel cabecera = Estilo.transparente(new BorderLayout());
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),
                Estilo.relleno(20, 26, 18, 26)));
        cabecera.add(titulo, BorderLayout.WEST);
        cabecera.add(lblCantidad, BorderLayout.EAST);

        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22) {
            @Override
            public Dimension getMinimumSize() {
                // encabezado de la tarjeta + encabezado de la tabla + las filas
                return new Dimension(100, cabecera.getPreferredSize().height + Tablas.altoContenido(tabla) + 2);
            }
        };
        tarjeta.setBorder(Estilo.relleno(1, 1, 1, 1));
        tarjeta.add(cabecera, BorderLayout.NORTH);
        tarjeta.add(Tablas.scroll(tabla), BorderLayout.CENTER);
        return tarjeta;
    }

    // ---------- fecha ----------

    private void cambiarFecha(LocalDate nueva) {
        fecha = nueva;
        String texto = FECHA.format(fecha);
        lblFecha.setText(Character.toUpperCase(texto.charAt(0)) + texto.substring(1));
        LocalDate hoy = LocalDate.now();
        if (fecha.equals(hoy)) {
            selDia.elegir(0);
        } else if (fecha.equals(hoy.minusDays(1))) {
            selDia.elegir(1);
        } else {
            selDia.limpiar();
        }
        btnSiguiente.setEnabled(fecha.isBefore(hoy)); // no hay cobros del futuro
        controlador.verReporte();
    }

    // ---------- lo que usa el controlador ----------

    public LocalDate getFecha() {
        return fecha;
    }

    public JButton getBotonVer() { return btnActualizar; }

    /**
     * Los totales vienen del backend como método → total; la lista de pedidos es del mismo día.
     */
    public void mostrarReporte(LocalDate dia, Map<String, BigDecimal> totales, List<DeliveryFila> pedidos) {
        if (!dia.equals(fecha)) {
            return; // llegó tarde la respuesta de otro día
        }
        BigDecimal total = totales.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        int cantidad = pedidos.size();
        datoTotal.setValor(Tablas.pesosCortos(total));
        datoPedidos.setValor(String.valueOf(cantidad));
        datoPromedio.setValor(cantidad == 0 ? "—"
                : Tablas.pesosCortos(total.divide(BigDecimal.valueOf(cantidad), 0, RoundingMode.HALF_UP)));
        datoEfectivo.setValor(Tablas.pesosCortos(totales.getOrDefault(MetodoPago.EFECTIVO.name(), BigDecimal.ZERO)));

        // las tres formas de pago siempre, aunque no haya cobros con alguna
        Map<MetodoPago, Integer> cantidades = new EnumMap<>(MetodoPago.class);
        pedidos.forEach(p -> {
            if (p.metodo() != null) {
                cantidades.merge(p.metodo(), 1, Integer::sum);
            }
        });
        panelMetodos.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        int fila = 0;
        for (MetodoPago m : MetodoPago.values()) {
            BigDecimal monto = totales.getOrDefault(m.name(), BigDecimal.ZERO);
            c.gridy = fila++;
            c.insets = new Insets(c.gridy == 0 ? 0 : 16, 0, 0, 0);
            panelMetodos.add(new FilaMetodo(CobrosPanel.texto(m), monto, cantidades.getOrDefault(m, 0), total), c);
        }

        // revalidate() del panel no relayoutea a los hijos: se invalida desde las filas nuevas
        panelMetodos.revalidate();
        modelo.setFilas(pedidos);
        lblCantidad.setText(cantidad + (cantidad == 1 ? " cobrado" : " cobrados"));
        revalidate();
        repaint();
    }

    /** "QR   $ 98.400 · 4 pedidos" y abajo una barra con su parte del total. */
    private static class FilaMetodo extends JPanel {
        FilaMetodo(String metodo, BigDecimal monto, int pedidos, BigDecimal total) {
            super(new BorderLayout(0, 8));
            setOpaque(false);

            JLabel nombre = new JLabel(metodo);
            nombre.setFont(Estilo.fuente(Font.PLAIN, 17f));
            nombre.setForeground(Estilo.TEXTO);

            JLabel importe = new JLabel(Tablas.pesosCortos(monto));
            importe.setFont(Estilo.fuente(Font.BOLD, 16f));
            importe.setForeground(monto.signum() == 0 ? Estilo.TEXTO_SUAVE : Estilo.TEXTO);
            JLabel cantidad = new JLabel(pedidos == 0 ? "" : " · " + pedidos + (pedidos == 1 ? " pedido" : " pedidos"));
            cantidad.setFont(Estilo.fuente(Font.PLAIN, 16f));
            cantidad.setForeground(Estilo.TEXTO_SUAVE);
            JPanel derecha = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            derecha.add(importe);
            derecha.add(cantidad);

            JPanel arriba = Estilo.transparente(new BorderLayout());
            arriba.add(nombre, BorderLayout.WEST);
            arriba.add(derecha, BorderLayout.EAST);

            double parte = total.signum() == 0 ? 0 : monto.doubleValue() / total.doubleValue();
            JComponent barra = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = Estilo.suave(g);
                    int alto = getHeight();
                    g2.setColor(Estilo.FILA_SELECCIONADA);
                    g2.fillRoundRect(0, 0, getWidth(), alto, alto, alto);
                    int lleno = (int) Math.round(getWidth() * parte);
                    if (lleno > 0) {
                        g2.setColor(Estilo.CAFE);
                        g2.fillRoundRect(0, 0, Math.max(lleno, alto), alto, alto, alto);
                    }
                    g2.dispose();
                }
            };
            barra.setPreferredSize(new Dimension(10, 12));

            add(arriba, BorderLayout.NORTH);
            add(barra, BorderLayout.CENTER);
        }
    }
}
