package org.example.escritorio.vista.admin;

import org.example.Modelo.persona.Sector;
import org.example.escritorio.controlador.admin.SectoresControlador;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Icono;
import org.example.escritorio.vista.comun.Interruptor;
import org.example.escritorio.vista.comun.PanelAbm;
import org.example.escritorio.vista.comun.Tablas;
import org.example.escritorio.vista.rrhh.EmpleadoTableModel;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

/**
 * Solapa Sectores de Administración (RF-52): alta, modificación, activar/desactivar y baja.
 * Cada sector muestra cuántos empleados tiene; solo se puede eliminar si está vacío.
 */
public class SectoresPanel extends PanelAbm<Sector> {

    private final SectoresControlador controlador;
    /** Empleados de cada sector (por id), lo manda el controlador antes de mostrar. */
    private Map<Integer, Long> empleadosPorSector = Map.of();

    public SectoresPanel() {
        super("Nuevo sector", 2, Disposicion.LISTA);
        controlador = new SectoresControlador(this);
        conectar(() -> controlador.cargar(null), controlador::nuevo, controlador::modificar,
                controlador::eliminar, controlador::alCambiarSeleccion);
        controlador.cargar(null);
    }

    @Override
    protected Comparator<Sector> orden() {
        return Comparator.comparing(s -> EmpleadoTableModel.textoSector(s.getNombre()));
    }

    @Override
    protected boolean mismo(Sector a, Sector b) {
        return a.getIdSector() == b.getIdSector();
    }

    @Override
    protected void mostrarResumen(String resumen) {
        long asignados = empleadosPorSector.values().stream().mapToLong(Long::longValue).sum();
        super.mostrarResumen(resumen + " · " + asignados + (asignados == 1 ? " empleado asignado" : " empleados asignados"));
    }

    @Override
    protected JComponent armarNota() {
        JLabel nota = new JLabel("Un sector con empleados no se puede eliminar: desactivalo con el interruptor. "
                + "El tacho aparece cuando el sector queda vacío.");
        nota.setIcon(new Icono(Icono.Tipo.INFO, 18, Estilo.TEXTO_SUAVE));
        nota.setIconTextGap(12);
        nota.setFont(Estilo.fuente(Font.PLAIN, 15f));
        nota.setForeground(Estilo.TEXTO);
        Estilo.Tarjeta caja = new Estilo.Tarjeta(new BorderLayout(), 16);
        caja.setBackground(Estilo.FILA_SELECCIONADA);
        caja.setBorder(Estilo.relleno(14, 20, 14, 20));
        caja.add(nota, BorderLayout.CENTER);
        return caja;
    }

    @Override
    protected JComponent crearFila(Sector sector, boolean elegida) {
        Tablas.Insignia colores = EmpleadoTableModel.insigniaSector(sector.getNombre());
        long empleados = empleadosPorSector.getOrDefault(sector.getIdSector(), 0L);

        JLabel nombre = new JLabel(EmpleadoTableModel.textoSector(sector.getNombre()));
        nombre.setFont(Estilo.fuente(Font.BOLD, 18f));
        nombre.setForeground(sector.isActiva() ? Estilo.TEXTO : Estilo.TEXTO_SUAVE);
        nombre.setBorder(Estilo.relleno(0, 0, 0, 6));
        JLabel detalle = new JLabel(empleados + (empleados == 1 ? " empleado" : " empleados")
                + (sector.isActiva() ? "" : " · inactivo"));
        detalle.setFont(Estilo.fuente(Font.PLAIN, 15f));
        detalle.setForeground(Estilo.TEXTO_SUAVE);
        JPanel textos = Estilo.transparente(null);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(nombre);
        textos.add(Box.createVerticalStrut(2));
        textos.add(detalle);

        Interruptor interruptor = new Interruptor(sector.isActiva());
        interruptor.setToolTipText(sector.isActiva() ? "Desactivar el sector" : "Activar el sector");
        interruptor.addActionListener(e -> sobre(sector, controlador::activarODesactivar));
        JButton editar = botonIcono(Icono.Tipo.LAPIZ, Estilo.TEXTO, "Modificar el nombre");
        editar.addActionListener(e -> modificar(sector));
        JPanel acciones = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.add(interruptor);
        acciones.add(editar);
        if (empleados == 0) {
            JButton borrar = botonIcono(Icono.Tipo.TACHO, Estilo.ROJO_TEXTO, "Eliminar el sector (está vacío)");
            borrar.addActionListener(e -> eliminar(sector));
            acciones.add(borrar);
        }

        JPanel fila = Estilo.transparente(new BorderLayout(16, 0));
        fila.add(new IconoSector(icono(sector.getNombre()), colores.fondo(), colores.color()), BorderLayout.WEST);
        fila.add(textos, BorderLayout.CENTER);
        fila.add(acciones, BorderLayout.EAST);
        return fila;
    }

    /** El mismo ícono que el área en el menú lateral. */
    private static Icono.Tipo icono(String sector) {
        return switch (sector == null ? "" : sector.toUpperCase(Locale.ROOT)) {
            case "ADMINISTRACION" -> Icono.Tipo.ENGRANAJE;
            case "BARRA" -> Icono.Tipo.COPA;
            case "CAJA" -> Icono.Tipo.BILLETE;
            case "COCINA" -> Icono.Tipo.OLLA;
            case "RECEPCION" -> Icono.Tipo.CALENDARIO;
            case "RECURSOSHUMANOS" -> Icono.Tipo.USUARIO;
            case "REPARTIDOR" -> Icono.Tipo.BICI;
            case "SALON" -> Icono.Tipo.MAS_CIRCULO;
            default -> Icono.Tipo.PERSONAS;
        };
    }

    // ---------- lo que usa el controlador ----------

    public void setEmpleadosPorSector(Map<Integer, Long> empleadosPorSector) {
        this.empleadosPorSector = empleadosPorSector;
    }

    /** El interruptor de cada fila hace de botón Activar; se deshabilita con Actualizar mientras guarda. */
    public JButton getBotonActivar() {
        return getBotonActualizar();
    }

    /** Antes había un botón Activar/Desactivar aparte; ahora cada fila tiene su interruptor. */
    public void mostrarBotonActivar(Sector sector) {
    }

    /** Cuadradito redondeado del color del sector, con su ícono. */
    private static class IconoSector extends JComponent {
        private final Icono icono;
        private final Color fondo;

        IconoSector(Icono.Tipo tipo, Color fondo, Color color) {
            this.icono = new Icono(tipo, 22, color);
            this.fondo = fondo;
            setPreferredSize(new Dimension(52, 52));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Estilo.suave(g);
            int lado = Math.min(getWidth(), getHeight());
            int y = (getHeight() - lado) / 2;
            g2.setColor(fondo);
            g2.fillRoundRect(0, y, lado, lado, 14, 14);
            g2.dispose();
            icono.paintIcon(this, g, (lado - icono.getIconWidth()) / 2, y + (lado - icono.getIconHeight()) / 2);
        }
    }
}
