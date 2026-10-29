package org.example.escritorio.vista.cocina;

import org.example.Modelo.enums.CategoriaMenu;
import org.example.Modelo.menu.Menu;
import org.example.escritorio.controlador.cocina.MenuEdicionControlador;
import org.example.escritorio.vista.comun.DialogoFormulario;
import org.example.escritorio.vista.comun.Formulario;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.Window;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

/**
 * Alta o modificación de un plato (RF-26). Con plato null es un alta.
 * Que el nombre no esté vacío y el precio sea mayor a 0 lo valida MenuService.
 */
public class MenuDialog extends DialogoFormulario {

    private final Menu plato;

    private final JTextField txtNombre = new JTextField(28);
    private final JTextField txtDescripcion = new JTextField(28);
    private final JSpinner spnPrecio = new JSpinner(new SpinnerNumberModel(0d, 0d, 10_000_000d, 100d));
    private final JCheckBox chkDisponible = new JCheckBox("Disponible para pedir", true);
    private final javax.swing.JComboBox<CategoriaMenu> cmbCategoria = new javax.swing.JComboBox<>(CategoriaMenu.values());

    private final MenuEdicionControlador controlador;
    private boolean guardado;

    public MenuDialog(Window padre, Menu plato) {
        super(padre, "Cocina · Menú",
                plato == null ? "Nuevo plato" : "Modificar plato",
                plato == null ? "Completá los datos del plato para sumarlo a la carta." : plato.getNombre(),
                "Guardar");
        this.plato = plato;

        spnPrecio.setEditor(new JSpinner.NumberEditor(spnPrecio, "#,##0.00"));

        if (plato != null) {
            txtNombre.setText(plato.getNombre());
            txtDescripcion.setText(plato.getDescripcion() == null ? "" : plato.getDescripcion());
            spnPrecio.setValue(plato.getPrecio() == null ? 0d : plato.getPrecio().doubleValue());
            cmbCategoria.setSelectedItem(plato.getCategoria());
            chkDisponible.setSelected(plato.isDisponible());
        }

        controlador = new MenuEdicionControlador(this);
        btnPrincipal.addActionListener(e -> controlador.guardar());

        armar();
    }

    @Override
    protected JComponent armarCuerpo() {
        Formulario.estilizar(txtNombre, "Ej.: Ñoquis de papa");
        Formulario.estilizar(txtDescripcion, "Ej.: Con salsa a elección");
        Formulario.estilizar(spnPrecio);
        Formulario.estilizar(cmbCategoria);
        return Formulario.cuerpo(
                Formulario.campo("Nombre", txtNombre, null),
                Formulario.campo("Descripción", txtDescripcion, "Opcional."),
                Formulario.enFila(
                        Formulario.campo("Categoría", cmbCategoria, "Las bebidas no van a la comanda."),
                        Formulario.campo("Precio ($)", spnPrecio, null)),
                Formulario.opcion(chkDisponible, "Si lo destildás, el plato no se puede cargar en los pedidos."));
    }

    // ---------- lo que usa el controlador ----------

    /** null si es un alta. */
    public Menu getPlato() { return plato; }
    public String getNombre() { return txtNombre.getText().trim(); }
    public String getDescripcion() { return txtDescripcion.getText().trim(); }
    public CategoriaMenu getCategoria() { return (CategoriaMenu) cmbCategoria.getSelectedItem(); }
    public boolean isDisponible() { return chkDisponible.isSelected(); }

    /** El precio como BigDecimal con 2 decimales; null si lo escrito no es un número. */
    public BigDecimal getPrecio() {
        try {
            spnPrecio.commitEdit(); // toma lo escrito aunque no se haya salido del campo
        } catch (ParseException e) {
            return null;
        }
        return BigDecimal.valueOf(((Number) spnPrecio.getValue()).doubleValue()).setScale(2, RoundingMode.HALF_UP);
    }

    public JButton getBotonGuardar() { return btnPrincipal; }

    public void cerrarGuardado() {
        guardado = true;
        dispose();
    }

    public boolean seGuardo() {
        return guardado;
    }
}
