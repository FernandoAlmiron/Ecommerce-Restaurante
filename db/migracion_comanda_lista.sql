-- =====================================================================
--  Agrega a Pedido la marca "listo" (la cocina termino la comanda)
--  a una base que ya existe (sin borrar datos).
--  No hace falta si se vuelve a ejecutar schema.sql y data.sql.
--  Ejecutar UNA vez, conectado como root.
-- =====================================================================
USE rest_reservas;

ALTER TABLE Pedido
    ADD COLUMN listo BOOLEAN NOT NULL DEFAULT FALSE AFTER observaciones;
