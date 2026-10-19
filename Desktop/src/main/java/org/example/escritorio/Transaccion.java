package org.example.escritorio;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Ejecuta una lectura dentro de una transacción de la base.
 *
 * Hace falta cuando se recorre una relación lazy de una entidad, por ejemplo los pedidos de un ticket:
 * esa lista se carga recién cuando se la usa, y fuera de una transacción da LazyInitializationException.
 * En la API no pasa porque la conexión sigue abierta mientras se arma el JSON; en Swing, sí.
 *
 *   ComandaDTO comanda = Transaccion.leer(() -> ComandaDTO.desde(ticketService.buscarPorId(nro)));
 *
 * Usarlo siempre dentro de una Tarea (es una consulta a la base).
 */
public final class Transaccion {

    private Transaccion() {
    }

    public static <T> T leer(Supplier<T> lectura) {
        TransactionTemplate transaccion = new TransactionTemplate(Contexto.get(PlatformTransactionManager.class));
        transaccion.setReadOnly(true);
        return transaccion.execute(estado -> lectura.get());
    }
}