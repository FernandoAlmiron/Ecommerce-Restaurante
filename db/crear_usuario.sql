-- =====================================================================
--  Usuario de MySQL que usa la aplicacion.
--  Ejecutar UNA vez, conectado como root, ANTES de schema.sql y data.sql.
--  La clave '1234' es la que la app usa por defecto (application.properties).
-- =====================================================================
CREATE USER IF NOT EXISTS 'restaurante_user'@'localhost' IDENTIFIED BY '1234';
ALTER USER 'restaurante_user'@'localhost' IDENTIFIED BY '1234';
GRANT ALL PRIVILEGES ON rest_reservas.* TO 'restaurante_user'@'localhost';
FLUSH PRIVILEGES;
