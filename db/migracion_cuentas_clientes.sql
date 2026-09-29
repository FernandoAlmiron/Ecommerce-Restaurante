-- =====================================================================
--  Agrega las cuentas de cliente y el metodo de pago del envio
--  a una base que ya existe (sin borrar datos).
--  No hace falta si se vuelve a ejecutar schema.sql y data.sql.
--  Ejecutar UNA vez, conectado como root.
-- =====================================================================
USE rest_reservas;

ALTER TABLE Clientes
    ADD COLUMN username VARCHAR(50) NULL UNIQUE AFTER email,
    ADD COLUMN password VARCHAR(100) NULL AFTER username;

ALTER TABLE Envio
    ADD COLUMN metodoPago ENUM('TARJETA','EFECTIVO','QR') NULL AFTER estado;

-- Cliente con cuenta de prueba: cliente / cliente123 (Maria Gonzalez)
UPDATE Clientes SET username = 'cliente', password = '$2a$10$eo2PWdGbNiwkGCTeDOMDeuOlZNUdLxrUgupekLSL2qdrJ4gXZWELW' WHERE dni = 30111222;
