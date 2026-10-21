package org.example.escritorio.vista.admin;

import org.example.Modelo.Restaurante;
import org.example.escritorio.controlador.admin.RestauranteControlador;
import org.example.escritorio.vista.comun.Dialogos;
import org.example.escritorio.vista.comun.Formulario;
import org.example.escritorio.vista.comun.FormularioSimple;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/** Solapa Restaurante de Administración (RF-49): datos del local y horario de atención. */
public class RestaurantePanel extends JPanel {

    private final JTextField txtNombre = new JTextField(28);
    private final JTextField txtDireccion = new JTextField(28);
    private final JTextField txtTelefono = new JTextField(16);
    private final JTextField txtEmail = new JTextField(28);
    private final JSpinner spnApertura = spinnerHora();
    private final JSpinner spnCierre = spinnerHora();
    private final JButton btnGuardar = new JButton("Guardar cambios");
    private final JButton btnDeshacer = new JButton("Deshacer cambios");

    public RestaurantePanel() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        FormularioSimple form = new FormularioSimple("Datos del restaurante")
                .fila("Nombre", txtNombre)
                .fila("Dirección", txtDireccion)
                .fila("Teléfono", txtTelefono)
                .fila("Email", txtEmail)
                .fila("Abre a las", spnApertura)
                .fila("Cierra a las", spnCierre)
                .nota("El horario se usa para aceptar reservas. Si cierra después de medianoche, "
                        + "poné la hora de cierre del día siguiente (por ejemplo, abre 19:00 y cierra 01:00).");

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botones.add(btnGuardar);
        botones.add(btnDeshacer);

        JPanel contenido = new JPanel(new BorderLayout(0, 16));
        contenido.add(form, BorderLayout.NORTH);
        contenido.add(botones, BorderLayout.CENTER);

        JPanel alineado = new JPanel(new BorderLayout()); // el formulario queda arriba a la izquierda
        alineado.add(contenido, BorderLayout.WEST);
        add(alineado, BorderLayout.NORTH);

        RestauranteControlador controlador = new RestauranteControlador(this);
        btnGuardar.addActionListener(e -> controlador.guardar());
        btnDeshacer.addActionListener(e -> controlador.cargar());
        controlador.cargar();
    }

    // ---------- lo que usa el controlador ----------

    public void mostrar(Restaurante r) {
        txtNombre.setText(r.getNombre());
        txtDireccion.setText(r.getDireccion());
        txtTelefono.setText(r.getTelefono());
        txtEmail.setText(r.getEmail());
        spnApertura.setValue(aFecha(r.getHorarioApertura()));
        spnCierre.setValue(aFecha(r.getHorarioCierre()));
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
}