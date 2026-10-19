package org.example.escritorio.vista.comun;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Function;

/**
 * Cómo se muestran pesos, fechas y horas en toda la app.
 * Los modelos de tabla devuelven los valores reales (LocalDate, BigDecimal...) para que ordenar
 * funcione bien, y estos renderers los muestran con formato.
 */
public final class Formatos {

    private static final Locale ARGENTINA = Locale.forLanguageTag("es-AR");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private Formatos() {
    }

    /** $ 30.000,00. Los montos son BigDecimal: nunca pasarlos a double. */
    public static String pesos(BigDecimal monto) {
        if (monto == null) {
            return "—";
        }
        NumberFormat formato = NumberFormat.getCurrencyInstance(ARGENTINA);
        return formato.format(monto);
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "—" : FECHA.format(fecha);
    }

    public static String hora(LocalTime hora) {
        return hora == null ? "—" : HORA.format(hora);
    }

    public static String siNo(Boolean valor) {
        if (valor == null) {
            return "—";
        }
        return valor ? "Sí" : "No";
    }

    /** Registra en la tabla cómo mostrar fechas, horas y montos. */
    public static void aplicarA(JTable tabla) {
        tabla.setDefaultRenderer(LocalDate.class, renderer(v -> fecha((LocalDate) v), false));
        tabla.setDefaultRenderer(LocalTime.class, renderer(v -> hora((LocalTime) v), false));
        tabla.setDefaultRenderer(BigDecimal.class, renderer(v -> pesos((BigDecimal) v), true));
    }

    private static DefaultTableCellRenderer renderer(Function<Object, String> texto,
                                                     boolean alineadoDerecha) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object valor) {
                setText(valor == null ? "—" : texto.apply(valor));
            }
        };
        if (alineadoDerecha) {
            r.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
        }
        return r;
    }
}