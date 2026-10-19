package org.example.escritorio.vista.comun;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * TableModel genérico para mostrar una lista de objetos en una JTable.
 * Cada tabla concreta solo define sus columnas: nombre, tipo y qué valor va en cada una.
 *
 * Importante: si la tabla tiene ordenamiento o filtro, la fila que ve el usuario no es la misma
 * que la del modelo. Convertir siempre con tabla.convertRowIndexToModel(...) antes de llamar a getFila.
 */
public abstract class ModeloTablaBase<T> extends AbstractTableModel {

    private final String[] columnas;
    private final List<T> filas = new ArrayList<>();

    protected ModeloTablaBase(String... columnas) {
        this.columnas = columnas;
    }

    /** Qué se muestra en la columna indicada para este objeto. */
    protected abstract Object valor(T fila, int columna);

    /** Tipo de cada columna (para ordenar y elegir cómo se muestra). Por defecto, texto. */
    protected Class<?> tipo(int columna) {
        return String.class;
    }

    public void setFilas(List<T> nuevas) {
        filas.clear();
        filas.addAll(nuevas);
        fireTableDataChanged();
    }

    public T getFila(int filaDelModelo) {
        return filas.get(filaDelModelo);
    }

    /** Reemplaza la primera fila que cumpla la condición (por ejemplo, la misma reserva ya actualizada). */
    public void reemplazar(Predicate<T> cual, T nueva) {
        for (int i = 0; i < filas.size(); i++) {
            if (cual.test(filas.get(i))) {
                filas.set(i, nueva);
                fireTableRowsUpdated(i, i);
                return;
            }
        }
    }

    @Override
    public int getRowCount() {
        return filas.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int columna) {
        return columnas[columna];
    }

    @Override
    public Class<?> getColumnClass(int columna) {
        return tipo(columna);
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        return valor(filas.get(fila), columna);
    }
}
