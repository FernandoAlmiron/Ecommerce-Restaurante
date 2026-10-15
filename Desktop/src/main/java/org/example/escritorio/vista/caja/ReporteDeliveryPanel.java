package org.example.escritorio.vista.caja;

import org.example.Modelo.enums.MetodoPago;
import org.example.escritorio.controlador.caja.ReporteDeliveryControlador;
import org.example.escritorio.vista.comun.Formatos;
import org.example.escritorio.vista.comun.ModeloTablaBase;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Reporte de delivery de un día (RF-48): lo cobrado en envíos, separado por método de pago.
 * Solo cuenta facturaciones pagadas; el cálculo lo hace FacturacionService.
 */
public class ReporteDeliveryPanel extends JPanel {

    /** Una fila del reporte: método y total. */
    private record Linea(String metodo, BigDecimal total) {
    }

    private final JSpinner spnFecha = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
    private final JButton btnVer = new JButton("Ver reporte");
    private final JLabel lblTitulo = new JLabel(" ");
    private final JLabel lblTotal = new JLabel(" ");

    private final ModeloTablaBase<Linea> modelo = new ModeloTablaBase<>("Método de pago", "Total cobrado") {
        @Override
        protected Object valor(Linea l, int columna) {
            return columna == 0 ? l.metodo() : l.total();
        }

        @Override
        protected Class<?> tipo(int columna) {
            return columna == 1 ? BigDecimal.class : String.class;
        }
    };
    private final JTable tabla = new JTable(modelo);

    private final ReporteDeliveryControlador controlador;

    public ReporteDeliveryPanel() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        spnFecha.setEditor(new JSpinner.DateEditor(spnFecha, "dd/MM/yyyy"));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.add(new JLabel("Día"));
        filtros.add(spnFecha);
        filtros.add(btnVer);

        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 15f));
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 15f));

        tabla.setRowHeight(28);
        tabla.setRowSelectionAllowed(false);
        tabla.setFillsViewportHeight(true);
        Formatos.aplicarA(tabla);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(420, 160));

        JPanel resultado = new JPanel(new BorderLayout(0, 8));
        resultado.add(lblTitulo, BorderLayout.NORTH);
        resultado.add(scroll, BorderLayout.CENTER);
        resultado.add(lblTotal, BorderLayout.SOUTH);

        JPanel izquierda = new JPanel(new BorderLayout());
        izquierda.add(resultado, BorderLayout.NORTH);

        add(filtros, BorderLayout.NORTH);
        add(izquierda, BorderLayout.WEST);

        controlador = new ReporteDeliveryControlador(this);
        btnVer.addActionListener(e -> controlador.verReporte());

        controlador.verReporte();
    }

    // ---------- lo que usa el controlador ----------

    public LocalDate getFecha() {
        Date fecha = (Date) spnFecha.getValue();
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public JButton getBotonVer() { return btnVer; }

    /** El reporte viene del backend como método → total. */
    public void mostrarReporte(LocalDate fecha, Map<String, BigDecimal> totales) {
        List<Linea> lineas = totales.entrySet().stream()
                .map(e -> new Linea(textoMetodo(e.getKey()), e.getValue()))
                .toList();
        modelo.setFilas(lineas);
        BigDecimal total = totales.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTitulo.setText("Delivery cobrado el " + Formatos.fecha(fecha));
        lblTotal.setText(lineas.isEmpty() ? "No hubo cobros de delivery ese día." : "Total: " + Formatos.pesos(total));
    }

    /** El backend devuelve el nombre del enum (EFECTIVO); se muestra como en el resto de la app. */
    private static String textoMetodo(String metodo) {
        try {
            return CobrosPanel.texto(MetodoPago.valueOf(metodo));
        } catch (IllegalArgumentException e) {
            return metodo;
        }
    }
}