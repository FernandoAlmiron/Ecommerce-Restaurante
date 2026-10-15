package org.example.escritorio;

import org.springframework.context.ApplicationContext;

/**
 * Acceso a los services del backend desde las clases de Swing.
 * Las ventanas no las crea Spring, así que no pueden recibir los services por constructor:
 * los piden acá.
 *
 *   ReservaService reservas = Contexto.get(ReservaService.class);
 */
public final class Contexto {

    private static ApplicationContext contexto;

    private Contexto() {
    }

    /** Lo llama AppEscritorio una sola vez, cuando Spring terminó de arrancar. */
    public static void iniciar(ApplicationContext contextoDeSpring) {
        contexto = contextoDeSpring;
    }

    public static <T> T get(Class<T> tipo) {
        if (contexto == null) {
            throw new IllegalStateException("Spring todavía no arrancó");
        }
        return contexto.getBean(tipo);
    }
}