package org.example.escritorio.vista.admin;

import org.example.Modelo.inventario.Proveedores;
import org.example.escritorio.vista.comun.ModeloTablaBase;

/** Columnas de la lista de proveedores (RF-56). */
public class ProveedorTableModel extends ModeloTablaBase<Proveedores> {

    public ProveedorTableModel() {
        super("Razón social", "CUIT", "Teléfono", "Email", "Categoría");
    }

    @Override
    protected Object valor(Proveedores p, int columna) {
        return switch (columna) {
            case 0 -> p.getRazonSocial();
            case 1 -> cuit(p.getCuit());
            case 2 -> texto(p.getTelefono());
            case 3 -> texto(p.getEmail());
            case 4 -> p.getCategoria() == null ? "" : p.getCategoria().getNombre();
            default -> null;
        };
    }

    /** El CUIT se guarda con 11 números; se muestra como 30-12345678-9. */
    public static String cuit(String cuit) {
        if (cuit == null || cuit.length() != 11) {
            return texto(cuit);
        }
        return cuit.substring(0, 2) + "-" + cuit.substring(2, 10) + "-" + cuit.substring(10);
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }
}