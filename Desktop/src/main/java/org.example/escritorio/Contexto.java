package org.example.escritorio;

import java.util.HashMap;
import java.util.Map;

/**
 * Lugar único donde las pantallas piden los servicios que usan:
 *   ServicioLogin login = Contexto.get(ServicioLogin.class);
 *
 * Por ahora los servicios se registran a mano en Main.
 * Cuando se separe el backend, get() se los va a pedir a Spring y las pantallas no cambian.
 */
public final class Contexto {

    private static final Map<Class<?>, Object> servicios = new HashMap<>();

    private Contexto() {
    }

    public static <T> void registrar(Class<T> tipo, T servicio) {
        servicios.put(tipo, servicio);
    }

    public static <T> T get(Class<T> tipo) {
        Object servicio = servicios.get(tipo);
        if (servicio == null) {
            throw new IllegalStateException("No hay ningún servicio registrado para " + tipo.getSimpleName());
        }
        return tipo.cast(servicio);
    }
}