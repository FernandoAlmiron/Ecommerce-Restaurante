-- =====================================================================
--  Agrega a Reserva los estados FINALIZADA (el cliente asistio) y
--  AUSENTE (no vino) a una base que ya existe (sin borrar datos).
--  Sin esto, en Recepcion fallan "Asistio" y "No asistio" con
--  "Data truncated for column 'estado'".
--  No hace falta si se vuelve a ejecutar schema.sql y data.sql.
--  Ejecutar UNA vez, conectado como root.
-- =====================================================================
USE rest_reservas;

ALTER TABLE Reserva
    MODIFY estado ENUM('PENDIENTE','CONFIRMADA','CANCELADA','FINALIZADA','AUSENTE') NOT NULL DEFAULT 'PENDIENTE';
