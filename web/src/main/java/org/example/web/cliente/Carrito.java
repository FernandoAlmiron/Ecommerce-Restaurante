package org.example.web.cliente;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
@Component /** de esta clase encargate vos: creá el objeto y dáselo a quien lo necesite */
@SessionScope /**  @SessionScope: Spring crea UN carrito por cada sesión (cada cliente logueado tiene el suyo) */
/**  Lo que se guarda es el ticket en la bd luego de pagar */

public class Carrito implements Serializable {

    // limites
    private static final int MAX_PLATOS_DISTINTOS = 10;
    private static final int MAX_UNIDADES_POR_PLATO = 10;

    // idMenu -> cantidad (LinkedHashMap mantiene el orden en que se agregaron)
    private final Map<Integer, Integer> items = new LinkedHashMap<>();
    private String nota = "";

    public void sumar(int idMenu) {
        int actual = cantidad(idMenu);
        if (actual == 0 && items.size() >= MAX_PLATOS_DISTINTOS) {
            throw new IllegalStateException("Un pedido admite hasta " + MAX_PLATOS_DISTINTOS + " platos distintos");
        }
        if (actual >= MAX_UNIDADES_POR_PLATO) {
            throw new IllegalStateException("Hasta " + MAX_UNIDADES_POR_PLATO + " unidades por plato");
        }
        items.put(idMenu, actual + 1);
    }

    public void restar(int idMenu) {
        int actual = cantidad(idMenu);
        if (actual <= 1) {
            items.remove(idMenu);      // si llega a 0, se saca del carrito
        } else {
            items.put(idMenu, actual - 1);
        }
    }

    // "volver a pedir": suma varias unidades de una vez, respetando los límites puestos

    public void agregar(int idMenu, int cantidad) {
        if (cantidad(idMenu) == 0 && items.size() >= MAX_PLATOS_DISTINTOS) {
            return;
        }
        items.put(idMenu, Math.min(MAX_UNIDADES_POR_PLATO, cantidad(idMenu) + cantidad));
    }

    public int cantidad(int idMenu) {
        return items.getOrDefault(idMenu, 0);
    }

    public Map<Integer, Integer> getItems() {
        return items;
    }

    public int getUnidades() {
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean isVacio() {
        return items.isEmpty();
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota == null ? "" : nota.trim();
    }

    public void vaciar() {
        items.clear();
        nota = "";
    }
}
