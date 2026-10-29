-- =====================================================================
--  Agrega a Menu la categoria del plato (Entradas, Pastas, Postres, Bebidas)
--  a una base que ya existe (sin borrar datos). Las bebidas no van a la
--  comanda de la cocina: las entrega el mozo.
--  Sirve aunque la app ya haya creado la columna sola (ddl-auto=update).
--  Tambien agrega la columna "eliminado" si falta (borrado logico de platos).
--  No hace falta si se vuelve a ejecutar schema.sql y data.sql.
--  Ejecutar UNA vez, conectado como root.
-- =====================================================================
USE rest_reservas;

-- 1) la columna categoria, si todavia no existe
SET @existe := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'Menu' AND COLUMN_NAME = 'categoria');
SET @sql := IF(@existe = 0,
    'ALTER TABLE Menu ADD COLUMN categoria ENUM(''ENTRADAS'',''PASTAS'',''POSTRES'',''BEBIDAS'') NULL',
    'SELECT 1');
PREPARE paso FROM @sql; EXECUTE paso; DEALLOCATE PREPARE paso;

-- 2) los platos sin categoria quedan como pastas, y la columna pasa a ser obligatoria
UPDATE Menu SET categoria = 'PASTAS' WHERE categoria IS NULL;
ALTER TABLE Menu MODIFY categoria ENUM('ENTRADAS','PASTAS','POSTRES','BEBIDAS') NOT NULL DEFAULT 'PASTAS';

-- 3) la categoria de los platos de data.sql (los demas se cambian desde Cocina > Menu y recetas)
UPDATE Menu SET categoria = 'POSTRES' WHERE nombre IN ('Tiramisu', 'Flan casero con dulce de leche');
UPDATE Menu SET categoria = 'BEBIDAS' WHERE nombre IN ('Agua mineral', 'Gaseosa', 'Copa de vino tinto', 'Cerveza artesanal');

-- 4) la columna eliminado, si todavia no existe
SET @existe := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'Menu' AND COLUMN_NAME = 'eliminado');
SET @sql := IF(@existe = 0,
    'ALTER TABLE Menu ADD COLUMN eliminado BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT 1');
PREPARE paso FROM @sql; EXECUTE paso; DEALLOCATE PREPARE paso;
