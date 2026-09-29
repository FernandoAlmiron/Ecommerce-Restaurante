-- =====================================================================
--  Contraseñas de los usuarios de prueba: usuario + 123 (admin -> admin123).
--  data.sql ya las trae. Este script sirve para una base que ya tiene datos
--  y no se quiere volver a cargar. Ejecutar conectado como root.
-- =====================================================================
USE rest_reservas;

UPDATE Empleados SET password = '$2a$10$R4fbj9GMY.tjjVDUKjPnPuSPgGCTiwCN0UjsnPuqbEGm0iQ0lOdcu' WHERE username = 'admin';
UPDATE Empleados SET password = '$2a$10$XfCg3CJ4E1FKA/eXcizfre1ojKV7NaOdJTttegQtR.7c3CCdbm.by' WHERE username = 'recepcion';
UPDATE Empleados SET password = '$2a$10$40.Aem6P//57AR8V2ygV2.lQgCSL6FdNnQS8UXw7BCeahFkxBcGjq' WHERE username = 'mozo';
UPDATE Empleados SET password = '$2a$10$AlH97WTnPimsSh6WQUgdkeAJeu3kNkfWq4Ix/Ab4TjYvBXPQeWRJ2' WHERE username = 'barra';
UPDATE Empleados SET password = '$2a$10$s7Mze6cQYW5tLs2ZZu9jEOqrlfv6g1NDML/KqzMPJJvZWeh5TjG2K' WHERE username = 'cocina';
UPDATE Empleados SET password = '$2a$10$kVjWVOZDTQWtQTpi6UnFsORTDB3hacabEIBSiyxCQa2//lx/MIRya' WHERE username = 'caja';
UPDATE Empleados SET password = '$2a$10$6Yl9VMIZQ/5mCt9Uf9cv9e8nMBTjC2BtvGcw.1X9249CSLssRad6u' WHERE username = 'repartidor';
UPDATE Empleados SET password = '$2a$10$mz8A9vbSVSOpxL3td3J83ONX1UC3KTrsmU1/NbIoI4ny.OaA90Vdy' WHERE username = 'rrhh';
