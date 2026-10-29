package org.example.Modelo.enums;

/**
 * Categoría de un plato de la carta. Ordena la carta al cargar pedidos y decide si el plato pasa por la cocina:
 * las bebidas las entrega el mozo, así que no van a la comanda.
 */
public enum CategoriaMenu {
    ENTRADAS("Entradas", true),
    PASTAS("Pastas", true),
    POSTRES("Postres", true),
    BEBIDAS("Bebidas", false);

    private final String nombre;
    private final boolean vaACocina;

    CategoriaMenu(String nombre, boolean vaACocina) {
        this.nombre = nombre;
        this.vaACocina = vaACocina;
    }

    /** Cómo se muestra ("Pastas"). */
    public String nombre() {
        return nombre;
    }

    /** false = no se prepara en la cocina (no aparece en la comanda). */
    public boolean vaACocina() {
        return vaACocina;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
