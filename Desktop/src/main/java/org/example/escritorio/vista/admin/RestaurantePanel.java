package org.example.escritorio.vista.admin;

import org.example.Modelo.Restaurante;
import org.example.escritorio.controlador.admin.RestauranteControlador;
import org.example.escritorio.vista.comun.Boton;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Estilo;
import org.example.escritorio.vista.comun.Formulario;
import org.example.escritorio.vista.comun.Icono;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Solapa Restaurante de Administración (RF-49): datos del local y horario de atención.
 * Abajo avisa si hay cambios sin guardar; Deshacer vuelve a traer lo guardado.
 */
public class RestaurantePanel extends JPanel {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final JTextField txtNombre = new JTextField(28);
    private final JTextField txtDireccion = new JTextField(28);
    private final JTextField txtTelefono = new JTextField(16);
    private final JTextField txtEmail = new JTextField(28);
    private final JSpinner spnApertura = spinnerHora();
    private final JSpinner spnCierre = spinnerHora();
    private final JButton btnGuardar = new Boton("Guardar cambios", Boton.Tipo.PRIMARIO);
    private final JButton btnDeshacer = new Boton("Deshacer cambios", Boton.Tipo.CONTORNO);

    private final LineaHorario lineaHorario = new LineaHorario();
    private final JLabel lblHorasAbierto = new JLabel(" ");
    private final JLabel lblHorario = new JLabel(" ");
    private final JLabel lblEstado = new JLabel(" ");
    private final Punto punto = new Punto();

    /** Lo último que se trajo de la base, para saber si hay cambios sin guardar. */
    private List<Object> guardado;

    public RestaurantePanel() {
        super(new BorderLayout(0, 22));
        setOpaque(false);
        setBorder(Estilo.relleno(4, 0, 0, 0));

        JPanel tarjetas = Estilo.transparente(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 0.56;
        c.insets = new Insets(0, 0, 0, 14);
        JComponent datos = armarDatos();
        JComponent horario = armarHorario();
        datos.setPreferredSize(new Dimension(100, datos.getPreferredSize().height));
        horario.setPreferredSize(new Dimension(100, horario.getPreferredSize().height));
        tarjetas.add(datos, c);
        c.gridx = 1;
        c.weightx = 0.44;
        c.insets = new Insets(0, 14, 0, 0);
        tarjetas.add(horario, c);

        JPanel arriba = Estilo.transparente(new BorderLayout());
        arriba.add(tarjetas, BorderLayout.NORTH);

        add(arriba, BorderLayout.CENTER);
        add(armarPie(), BorderLayout.SOUTH);

        RestauranteControlador controlador = new RestauranteControlador(this);
        btnGuardar.addActionListener(e -> controlador.guardar());
        btnDeshacer.addActionListener(e -> controlador.cargar());

        // cualquier cambio en los campos actualiza el aviso y la línea del horario
        DocumentListener alEscribir = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { alCambiar(); }
            @Override public void removeUpdate(DocumentEvent e) { alCambiar(); }
            @Override public void changedUpdate(DocumentEvent e) { alCambiar(); }
        };
        for (JTextField t : new JTextField[]{txtNombre, txtDireccion, txtTelefono, txtEmail}) {
            t.getDocument().addDocumentListener(alEscribir);
        }
        spnApertura.addChangeListener(e -> alCambiar());
        spnCierre.addChangeListener(e -> alCambiar());

        alCambiar();
        controlador.cargar();
    }

    // ---------- armado ----------

    private JComponent armarDatos() {
        JLabel titulo = new JLabel("Datos del restaurante");
        titulo.setFont(Estilo.fuente(Font.BOLD, 22f));
        titulo.setForeground(Estilo.TEXTO);
        JLabel subtitulo = Formulario.nota(new JLabel("Aparecen en los tickets y en los comprobantes."));
        JPanel textos = Estilo.transparente(null);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(subtitulo);
        JPanel cabecera = Estilo.transparente(new BorderLayout(16, 0));
        cabecera.add(new Logo(), BorderLayout.WEST);
        cabecera.add(textos, BorderLayout.CENTER);

        Formulario.estilizar(txtNombre, null);
        Formulario.estilizar(txtDireccion, "Calle, número y localidad");
        Formulario.estilizar(txtTelefono, null);
        Formulario.estilizar(txtEmail, null);
        txtDireccion.putClientProperty("JTextField.leadingIcon", new Icono(Icono.Tipo.UBICACION, 18, Estilo.TEXTO_SUAVE));
        txtTelefono.putClientProperty("JTextField.leadingIcon", new Icono(Icono.Tipo.TELEFONO, 18, Estilo.TEXTO_SUAVE));
        txtEmail.putClientProperty("JTextField.leadingIcon", new Icono(Icono.Tipo.SOBRE, 18, Estilo.TEXTO_SUAVE));

        JPanel cuerpo = Formulario.cuerpo(
                cabecera,
                Formulario.campo("Nombre", txtNombre, null),
                Formulario.campo("Dirección", txtDireccion, null),
                Formulario.enFila(
                        Formulario.campo("Teléfono", txtTelefono, null),
                        Formulario.campo("Email", txtEmail, null)));
        return tarjeta(cuerpo);
    }

    private JComponent armarHorario() {
        JLabel titulo = new JLabel("Horario de atención");
        titulo.setFont(Estilo.fuente(Font.BOLD, 22f));
        titulo.setForeground(Estilo.TEXTO);
        JLabel subtitulo = Formulario.nota(new JLabel("Se usa para aceptar reservas."));
        JPanel textos = Estilo.transparente(null);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(subtitulo);

        for (JSpinner s : new JSpinner[]{spnApertura, spnCierre}) {
            Formulario.estilizar(s);
            s.setFont(Estilo.fuente(Font.BOLD, 20f));
        }

        // la línea del día con la franja abierta
        lblHorasAbierto.setFont(Estilo.fuente(Font.BOLD, 16f));
        lblHorasAbierto.setForeground(Estilo.TEXTO);
        lblHorario.setFont(Estilo.fuente(Font.PLAIN, 16f));
        lblHorario.setForeground(Estilo.TEXTO_SUAVE);
        JPanel resumenHorario = Estilo.transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        resumenHorario.add(lblHorasAbierto);
        resumenHorario.add(lblHorario);
        Estilo.Tarjeta caja = new Estilo.Tarjeta(new BorderLayout(0, 10), 16);
        caja.setBackground(Estilo.FILA_SELECCIONADA);
        caja.setBorder(Estilo.relleno(14, 20, 16, 20));
        caja.add(lineaHorario, BorderLayout.CENTER);
        caja.add(resumenHorario, BorderLayout.SOUTH);

        JLabel ayuda = Formulario.nota(new JLabel("<html>Si cierra después de medianoche, poné la hora del día "
                + "siguiente (abre 19:00, cierra 01:00).</html>"));
        ayuda.setIcon(new Icono(Icono.Tipo.INFO, 18, Estilo.TEXTO_SUAVE));
        ayuda.setIconTextGap(10);

        JPanel cuerpo = Formulario.cuerpo(
                textos,
                Formulario.enFila(
                        Formulario.campo("Abre a las", spnApertura, null),
                        Formulario.campo("Cierra a las", spnCierre, null)),
                caja,
                ayuda);
        return tarjeta(cuerpo);
    }

    private static JComponent tarjeta(JComponent cuerpo) {
        Estilo.Tarjeta tarjeta = new Estilo.Tarjeta(new BorderLayout(), 22);
        tarjeta.setBorder(Estilo.relleno(26, 30, 28, 30));
        tarjeta.add(cuerpo, BorderLayout.NORTH);
        return tarjeta;
    }

    /** Barra de abajo: si hay cambios sin guardar, y los botones. */
    private JComponent armarPie() {
        lblEstado.setFont(Estilo.fuente(Font.PLAIN, 16f));
        lblEstado.setForeground(Estilo.TEXTO);
        JPanel estado = Estilo.transparente(new FlowLayout(FlowLayout.LEFT, 10, 0));
        estado.add(punto);
        estado.add(lblEstado);
        JPanel estadoCentrado = Estilo.transparente(new GridBagLayout());
        estadoCentrado.add(estado);

        btnDeshacer.setBorder(Estilo.relleno(13, 22, 13, 22));
        btnGuardar.setBorder(Estilo.relleno(13, 26, 13, 26));
        JPanel botones = Estilo.transparente(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        botones.add(btnDeshacer);
        botones.add(btnGuardar);

        Estilo.Tarjeta pie = new Estilo.Tarjeta(new BorderLayout(), 22);
        pie.setBorder(Estilo.relleno(14, 18, 14, 18));
        pie.add(estadoCentrado, BorderLayout.WEST);
        pie.add(botones, BorderLayout.EAST);
        return pie;
    }

    // ---------- cambios ----------

    /** Lo que hay en pantalla, para comparar con lo guardado. */
    private List<Object> enPantalla() {
        return List.of(txtNombre.getText().trim(), txtDireccion.getText().trim(), txtTelefono.getText().trim(),
                txtEmail.getText().trim(), Objects.toString(leer(spnApertura)), Objects.toString(leer(spnCierre)));
    }

    private void alCambiar() {
        LocalTime abre = leer(spnApertura);
        LocalTime cierra = leer(spnCierre);
        lineaHorario.setHorario(abre, cierra);
        if (abre != null && cierra != null && !abre.equals(cierra)) {
            long minutos = Duration.between(abre, cierra).toMinutes();
            if (minutos <= 0) {
                minutos += 24 * 60; // cierra al día siguiente
            }
            long horas = minutos / 60;
            long resto = minutos % 60;
            lblHorasAbierto.setText("Abierto " + horas + " h" + (resto > 0 ? " " + resto + " min" : ""));
            lblHorario.setText(" · de " + HORA.format(abre) + " a " + HORA.format(cierra));
        } else {
            lblHorasAbierto.setText("Revisá el horario");
            lblHorario.setText("");
        }

        boolean cambios = guardado != null && !guardado.equals(enPantalla());
        punto.setVisible(cambios);
        lblEstado.setText(guardado == null ? "Cargando..." : cambios ? "Tenés cambios sin guardar" : "Todo guardado");
        lblEstado.setForeground(cambios ? Estilo.TEXTO : Estilo.TEXTO_SUAVE);
        btnGuardar.setEnabled(cambios);
        btnDeshacer.setEnabled(cambios);
    }

    // ---------- lo que usa el controlador ----------

    public void mostrar(Restaurante r) {
        txtNombre.setText(r.getNombre());
        txtDireccion.setText(r.getDireccion());
        txtTelefono.setText(r.getTelefono());
        txtEmail.setText(r.getEmail());
        for (JTextField t : new JTextField[]{txtNombre, txtDireccion, txtTelefono, txtEmail}) {
            t.setCaretPosition(0); // que se vea el principio, no el final
        }
        spnApertura.setValue(aFecha(r.getHorarioApertura()));
        spnCierre.setValue(aFecha(r.getHorarioCierre()));
        guardado = enPantalla();
        alCambiar();
    }

    public String getNombre() { return txtNombre.getText().trim(); }
    public String getDireccion() { return txtDireccion.getText().trim(); }
    public String getTelefono() { return txtTelefono.getText().trim(); }
    public String getEmail() { return txtEmail.getText().trim(); }
    /** null si lo escrito no es una hora válida. */
    public LocalTime getApertura() { return leer(spnApertura); }
    public LocalTime getCierre() { return leer(spnCierre); }

    public JButton getBotonGuardar() { return btnGuardar; }
    public JButton getBotonDeshacer() { return btnDeshacer; }

    public void mostrarError(String mensaje) { Dialogos.error(this, mensaje); }
    public void mostrarInfo(String mensaje) { Dialogos.info(this, mensaje); }

    // ---------- horas ----------

    private static JSpinner spinnerHora() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "HH:mm"));
        return spinner;
    }

    private static Date aFecha(LocalTime hora) {
        LocalTime h = hora == null ? LocalTime.of(12, 0) : hora;
        return Date.from(h.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
    }

    private static LocalTime leer(JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (ParseException e) {
            return null;
        }
        return LocalTime.ofInstant(((Date) spinner.getValue()).toInstant(), ZoneId.systemDefault())
                .withSecond(0).withNano(0);
    }

    // ---------- piezas visuales ----------

    /** Las 24 horas del día con la franja en que está abierto (si cierra después de medianoche, son dos tramos). */
    private static class LineaHorario extends JComponent {
        private static final int[] MARCAS = {0, 6, 12, 18, 24};
        private LocalTime abre;
        private LocalTime cierra;

        LineaHorario() {
            setFont(Estilo.fuente(Font.PLAIN, 14f));
            setPreferredSize(new Dimension(300, 44));
        }

        void setHorario(LocalTime abre, LocalTime cierra) {
            this.abre = abre;
            this.cierra = cierra;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Estilo.suave(g);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int ancho = getWidth();
            g2.setColor(Estilo.TEXTO_SUAVE);
            for (int h : MARCAS) {
                String texto = String.format("%02d", h);
                int x = (int) Math.round(ancho * h / 24.0) - fm.stringWidth(texto) / 2;
                x = Math.max(0, Math.min(ancho - fm.stringWidth(texto), x));
                g2.drawString(texto, x, fm.getAscent());
            }
            int y = fm.getHeight() + 8;
            int alto = 14;
            g2.setColor(new Color(0xE2DAD2));
            g2.fillRoundRect(0, y, ancho, alto, alto, alto);
            if (abre != null && cierra != null && !abre.equals(cierra)) {
                g2.setColor(Estilo.CAFE);
                double desde = minutos(abre) / 1440.0;
                double hasta = minutos(cierra) / 1440.0;
                if (hasta > desde) {
                    tramo(g2, desde, hasta, y, alto, ancho);
                } else { // cruza la medianoche
                    tramo(g2, desde, 1, y, alto, ancho);
                    tramo(g2, 0, hasta, y, alto, ancho);
                }
            }
            g2.dispose();
        }

        private static void tramo(Graphics2D g2, double desde, double hasta, int y, int alto, int ancho) {
            int x1 = (int) Math.round(ancho * desde);
            int x2 = (int) Math.round(ancho * hasta);
            if (x2 > x1) {
                g2.fillRoundRect(x1, y, Math.max(x2 - x1, alto), alto, alto, alto);
            }
        }

        private static int minutos(LocalTime t) {
            return t.getHour() * 60 + t.getMinute();
        }
    }

    /** Cuadrado café con las iniciales del logo ("LN") en letra manuscrita. */
    private static class Logo extends JComponent {
        Logo() {
            setPreferredSize(new Dimension(64, 64));
            setFont(Estilo.fuenteLogo(26f));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Estilo.suave(g);
            g2.setColor(Estilo.CAFE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.setColor(Color.WHITE);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String texto = "LN";
            g2.drawString(texto, (getWidth() - fm.stringWidth(texto)) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Puntito amarillo del aviso de cambios. */
    private static class Punto extends JComponent {
        Punto() {
            setPreferredSize(new Dimension(10, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Estilo.suave(g);
            g2.setColor(new Color(0xE0A100));
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.dispose();
        }
    }
}
