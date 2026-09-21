-- =====================================================================
--  rest_reservas  -  registros de ejemplo: restaurante de pastas caseras
--  Se corre DESPUES de schema.sql (que deja todas las tablas vacias).
--  Los nombres, CUIT, DNI, telefonos y mails son inventados.
--  Las fechas de reservas, tickets y facturas son relativas a HOY (CURDATE),
--  asi que el script sirve el dia que lo corras.
-- =====================================================================
USE rest_reservas;

-- ---------- Restaurante (abre 11:00, cierra 00:00: cierre pasada la medianoche) ----------
INSERT INTO Restaurante (nombre, direccion, telefono, email, horarioApertura, horarioCierre) VALUES
    ('La Pasta de la Nonna', 'Av. Corrientes 1234, CABA', '1145551234', 'contacto@lapastadelanonna.example.com', '11:00:00', '00:00:00');

-- ---------- Sectores: los nombres tienen que ser EXACTOS, el login los usa para dar el rol ----------
INSERT INTO Sector (nombre, activa, nroRestaurante) VALUES
    ('ADMINISTRACION',  TRUE, 1),
    ('COCINA',          TRUE, 1),
    ('SALON',           TRUE, 1),
    ('BARRA',           TRUE, 1),
    ('CAJA',            TRUE, 1),
    ('REPARTIDOR',      TRUE, 1),
    ('RECEPCION',       TRUE, 1),
    ('RECURSOSHUMANOS', TRUE, 1);

-- ---------- Zonas (el Patio esta inactiva a proposito, para probar la validacion) ----------
INSERT INTO Zona (nombre, capacidadMaxima, activa, nroRestaurante) VALUES
    ('Salon principal', 40, TRUE,  1),
    ('Terraza',         20, TRUE,  1),
    ('Salon privado',   12, TRUE,  1),
    ('Patio',           16, FALSE, 1);

-- ---------- Empleados (una cuenta por rol; las claves estan en el chat, aca solo van los hashes BCrypt) ----------
INSERT INTO Empleados (nombre, apellido, celular, dni, username, password, idSector) VALUES
    ('Laura', 'Benitez', '1155552001', 25111001, 'admin', '$2a$10$khWyXKHxYzDR24EXFNxPue/pho9VOY/VVC/O9pzqKlvnaC.KaMgKq', (SELECT idSector FROM Sector WHERE nombre = 'ADMINISTRACION')),
    ('Camila', 'Suarez', '1155552002', 36111002, 'recepcion', '$2a$10$Pp4yBnwyDnvL9CDK8HQJie7s9UN6XD07JArJqsoLgkvSNkG6xTp6a', (SELECT idSector FROM Sector WHERE nombre = 'RECEPCION')),
    ('Martin', 'Acosta', '1155552003', 34111003, 'mozo', '$2a$10$HxKxdDDlyBJ5BlfCg4Zsiue7wS9DpJ8t3i5ZY0LELEA6oUyrrWg5i', (SELECT idSector FROM Sector WHERE nombre = 'SALON')),
    ('Julieta', 'Romero', '1155552004', 38111004, 'barra', '$2a$10$aHKX1QQV93ZVOVlIFQf06OsY9bxgT0yBLRZ8sATimP9diA8egvC6u', (SELECT idSector FROM Sector WHERE nombre = 'BARRA')),
    ('Gustavo', 'Ferrari', '1155552005', 29111005, 'cocina', '$2a$10$SwuL5J0QHKmu111UVB5uz.1KmD4xpXpdF6q3F78BwATYEodDzaH1G', (SELECT idSector FROM Sector WHERE nombre = 'COCINA')),
    ('Valeria', 'Molina', '1155552006', 33111006, 'caja', '$2a$10$iXnoVryY3D846UPUJkDOtuPm6foSHVmKowiVzvA/36HpDMRTKMs4m', (SELECT idSector FROM Sector WHERE nombre = 'CAJA')),
    ('Nicolas', 'Vega', '1155552007', 37111007, 'repartidor', '$2a$10$MXeJfmBSlOdBpSpfXVIRZe1wHgq/E9QuoNnHQm9r/MS4pQI4UtjFW', (SELECT idSector FROM Sector WHERE nombre = 'REPARTIDOR')),
    ('Patricia', 'Duarte', '1155552008', 31111008, 'rrhh', '$2a$10$VDQZITKYThvWUmSa7tz7uOkGTzXM2NbV.k0TDJFDBrxZ3KYzYg/Iq', (SELECT idSector FROM Sector WHERE nombre = 'RECURSOSHUMANOS'));

-- ---------- Categorias y proveedores ----------
INSERT INTO Categoria (nombre, descripcion, activo) VALUES
    ('Harinas y masas',    'Harinas, semola, huevos y papa para las masas', TRUE),
    ('Lacteos y quesos',   'Quesos, cremas, manteca y leche',               TRUE),
    ('Carnes y fiambres',  'Carne picada, pollo, jamon y panceta',          TRUE),
    ('Verduras y hierbas', 'Verduras frescas y hierbas',                    TRUE),
    ('Salsas y almacen',   'Tomate, aceite, dulce de leche y secos',        TRUE),
    ('Bebidas',            'Aguas, gaseosas, vinos y cervezas',             TRUE);

INSERT INTO Proveedores (cuit, razonSocial, telefono, email, nroCategoria) VALUES
    ('30711223344', 'Molino La Espiga SA',          '1143001100', 'ventas@laespiga.example.com',    (SELECT nroCategoria FROM Categoria WHERE nombre = 'Harinas y masas')),
    ('30722334455', 'Lacteos del Valle SRL',        '1143002200', 'pedidos@lacteosvalle.example.com',(SELECT nroCategoria FROM Categoria WHERE nombre = 'Lacteos y quesos')),
    ('30733445566', 'Carniceria Don Pepe',          '1143003300', 'donpepe@example.com',            (SELECT nroCategoria FROM Categoria WHERE nombre = 'Carnes y fiambres')),
    ('30744556677', 'Verduleria Los Andes',         '1143004400', 'losandes@example.com',           (SELECT nroCategoria FROM Categoria WHERE nombre = 'Verduras y hierbas')),
    ('30755667788', 'Almacen Central SRL',          '1143005500', 'compras@almacencentral.example.com',(SELECT nroCategoria FROM Categoria WHERE nombre = 'Salsas y almacen')),
    ('30766778899', 'Distribuidora de Bebidas Sur', '1143006600', 'pedidos@bebidassur.example.com', (SELECT nroCategoria FROM Categoria WHERE nombre = 'Bebidas'));

-- ---------- Stock (cantidades en kg, lt o unidades segun unidadMedida) ----------
INSERT INTO Stock (nombre, unidadMedida, cantidadActual, stockMinimo, stockMinimoActivo, nroRestaurante, nroCategoria)
SELECT x.nombre, x.unidad, x.actual, x.minimo, TRUE, 1, c.nroCategoria
FROM (
    SELECT 'Harina 000' AS nombre, 'kg' AS unidad, 60.0 AS actual, 15.0 AS minimo, 'Harinas y masas' AS categoria
    UNION ALL SELECT 'Semola',            'kg',      25.0,  8.0, 'Harinas y masas'
    UNION ALL SELECT 'Huevos',            'unidad', 240.0, 60.0, 'Harinas y masas'
    UNION ALL SELECT 'Papa',              'kg',      40.0, 10.0, 'Harinas y masas'
    UNION ALL SELECT 'Ricota',            'kg',      15.0,  4.0, 'Lacteos y quesos'
    UNION ALL SELECT 'Mozzarella',        'kg',      12.0,  3.0, 'Lacteos y quesos'
    UNION ALL SELECT 'Queso parmesano',   'kg',       6.0,  1.5, 'Lacteos y quesos'
    UNION ALL SELECT 'Crema de leche',    'lt',      14.0,  4.0, 'Lacteos y quesos'
    UNION ALL SELECT 'Manteca',           'kg',       5.0,  1.0, 'Lacteos y quesos'
    UNION ALL SELECT 'Mascarpone',        'kg',       5.0,  1.0, 'Lacteos y quesos'
    UNION ALL SELECT 'Leche',             'lt',      20.0,  5.0, 'Lacteos y quesos'
    UNION ALL SELECT 'Carne picada',      'kg',      14.0,  4.0, 'Carnes y fiambres'
    UNION ALL SELECT 'Jamon cocido',      'kg',       8.0,  2.0, 'Carnes y fiambres'
    UNION ALL SELECT 'Panceta',           'kg',       6.0,  1.5, 'Carnes y fiambres'
    UNION ALL SELECT 'Pollo',             'kg',      10.0,  3.0, 'Carnes y fiambres'
    UNION ALL SELECT 'Espinaca',          'kg',       9.0,  2.0, 'Verduras y hierbas'
    UNION ALL SELECT 'Hongos',            'kg',       6.0,  1.5, 'Verduras y hierbas'
    UNION ALL SELECT 'Cebolla',           'kg',      15.0,  4.0, 'Verduras y hierbas'
    UNION ALL SELECT 'Albahaca',          'kg',       1.0,  0.2, 'Verduras y hierbas'
    UNION ALL SELECT 'Ajo',               'kg',       2.0,  0.5, 'Verduras y hierbas'
    UNION ALL SELECT 'Tomate triturado',  'kg',      30.0,  8.0, 'Salsas y almacen'
    UNION ALL SELECT 'Aceite de oliva',   'lt',      12.0,  3.0, 'Salsas y almacen'
    UNION ALL SELECT 'Dulce de leche',    'kg',       6.0,  1.5, 'Salsas y almacen'
    UNION ALL SELECT 'Cafe',              'kg',       2.0,  0.5, 'Salsas y almacen'
    UNION ALL SELECT 'Nueces',            'kg',       2.0,  0.5, 'Salsas y almacen'
    UNION ALL SELECT 'Agua mineral',      'unidad', 120.0, 30.0, 'Bebidas'
    UNION ALL SELECT 'Gaseosa',           'unidad',  96.0, 24.0, 'Bebidas'
    UNION ALL SELECT 'Vino tinto',        'botella', 36.0, 12.0, 'Bebidas'
    UNION ALL SELECT 'Cerveza',           'unidad',  72.0, 24.0, 'Bebidas'
) x JOIN Categoria c ON c.nombre = x.categoria;

-- ---------- Menu (el ultimo esta NO disponible a proposito, para probar la validacion) ----------
INSERT INTO Menu (nombre, descripcion, precio, disponible, nroRestaurante) VALUES
    ('Ravioles de ricota y espinaca con fileto', 'Pasta rellena casera con salsa de tomate',          13500.00, TRUE,  1),
    ('Sorrentinos de jamon y queso con crema',   'Sorrentinos caseros con salsa de crema',            15200.00, TRUE,  1),
    ('Noquis de papa con bolognesa',             'Noquis caseros con salsa de carne',                 12800.00, TRUE,  1),
    ('Ravioles a la carbonara',                  'Ravioles de ricota con panceta, crema y parmesano', 14600.00, TRUE,  1),
    ('Tallarines caseros al pesto',              'Tallarines con albahaca, nueces y parmesano',       12500.00, TRUE,  1),
    ('Fettuccine con hongos y crema',            'Fettuccine caseros con hongos salteados',           14800.00, TRUE,  1),
    ('Lasagna de carne',                         'Lasagna casera gratinada',                          16500.00, TRUE,  1),
    ('Canelones de pollo y verdura',             'Canelones caseros con salsa blanca y fileto',       14500.00, TRUE,  1),
    ('Tiramisu',                                 'Postre italiano de mascarpone y cafe',               7800.00, TRUE,  1),
    ('Flan casero con dulce de leche',           'Flan con dulce de leche',                            6900.00, TRUE,  1),
    ('Agua mineral',                             'Botella 500 cc',                                     3200.00, TRUE,  1),
    ('Gaseosa',                                  'Botella 500 cc',                                     3800.00, TRUE,  1),
    ('Copa de vino tinto',                       'Copa de vino tinto de la casa',                      5500.00, TRUE,  1),
    ('Cerveza artesanal',                        'Botella 473 cc',                                     5200.00, TRUE,  1),
    ('Sorrentinos de calabaza y nuez',           'Plato de temporada, hoy sin stock de calabaza',     15000.00, FALSE, 1);

-- ---------- Recetas: cantidad de cada ingrediente por porcion (misma unidad que el stock) ----------
INSERT INTO MenuIngrediente (idMenu, idStock, cantidadNecesaria)
SELECT m.idMenu, s.idStock, r.cantidad
FROM (
    SELECT 'Ravioles de ricota y espinaca con fileto' AS menu, 'Harina 000' AS ingrediente, 0.120 AS cantidad
    UNION ALL SELECT 'Ravioles de ricota y espinaca con fileto', 'Huevos', 1
    UNION ALL SELECT 'Ravioles de ricota y espinaca con fileto', 'Ricota', 0.100
    UNION ALL SELECT 'Ravioles de ricota y espinaca con fileto', 'Espinaca', 0.060
    UNION ALL SELECT 'Ravioles de ricota y espinaca con fileto', 'Tomate triturado', 0.150
    UNION ALL SELECT 'Ravioles de ricota y espinaca con fileto', 'Aceite de oliva', 0.010
    UNION ALL SELECT 'Ravioles de ricota y espinaca con fileto', 'Ajo', 0.005
    UNION ALL SELECT 'Sorrentinos de jamon y queso con crema', 'Harina 000', 0.120
    UNION ALL SELECT 'Sorrentinos de jamon y queso con crema', 'Huevos', 1
    UNION ALL SELECT 'Sorrentinos de jamon y queso con crema', 'Jamon cocido', 0.080
    UNION ALL SELECT 'Sorrentinos de jamon y queso con crema', 'Mozzarella', 0.080
    UNION ALL SELECT 'Sorrentinos de jamon y queso con crema', 'Crema de leche', 0.100
    UNION ALL SELECT 'Noquis de papa con bolognesa', 'Papa', 0.300
    UNION ALL SELECT 'Noquis de papa con bolognesa', 'Harina 000', 0.100
    UNION ALL SELECT 'Noquis de papa con bolognesa', 'Huevos', 1
    UNION ALL SELECT 'Noquis de papa con bolognesa', 'Carne picada', 0.100
    UNION ALL SELECT 'Noquis de papa con bolognesa', 'Tomate triturado', 0.150
    UNION ALL SELECT 'Noquis de papa con bolognesa', 'Cebolla', 0.050
    UNION ALL SELECT 'Noquis de papa con bolognesa', 'Queso parmesano', 0.020
    UNION ALL SELECT 'Ravioles a la carbonara', 'Harina 000', 0.120
    UNION ALL SELECT 'Ravioles a la carbonara', 'Huevos', 2
    UNION ALL SELECT 'Ravioles a la carbonara', 'Ricota', 0.100
    UNION ALL SELECT 'Ravioles a la carbonara', 'Panceta', 0.060
    UNION ALL SELECT 'Ravioles a la carbonara', 'Queso parmesano', 0.030
    UNION ALL SELECT 'Ravioles a la carbonara', 'Crema de leche', 0.060
    UNION ALL SELECT 'Tallarines caseros al pesto', 'Harina 000', 0.100
    UNION ALL SELECT 'Tallarines caseros al pesto', 'Semola', 0.050
    UNION ALL SELECT 'Tallarines caseros al pesto', 'Huevos', 1
    UNION ALL SELECT 'Tallarines caseros al pesto', 'Albahaca', 0.020
    UNION ALL SELECT 'Tallarines caseros al pesto', 'Queso parmesano', 0.030
    UNION ALL SELECT 'Tallarines caseros al pesto', 'Nueces', 0.020
    UNION ALL SELECT 'Tallarines caseros al pesto', 'Aceite de oliva', 0.030
    UNION ALL SELECT 'Tallarines caseros al pesto', 'Ajo', 0.005
    UNION ALL SELECT 'Fettuccine con hongos y crema', 'Harina 000', 0.130
    UNION ALL SELECT 'Fettuccine con hongos y crema', 'Huevos', 1
    UNION ALL SELECT 'Fettuccine con hongos y crema', 'Hongos', 0.120
    UNION ALL SELECT 'Fettuccine con hongos y crema', 'Crema de leche', 0.100
    UNION ALL SELECT 'Fettuccine con hongos y crema', 'Manteca', 0.015
    UNION ALL SELECT 'Fettuccine con hongos y crema', 'Ajo', 0.005
    UNION ALL SELECT 'Lasagna de carne', 'Harina 000', 0.120
    UNION ALL SELECT 'Lasagna de carne', 'Huevos', 1
    UNION ALL SELECT 'Lasagna de carne', 'Carne picada', 0.150
    UNION ALL SELECT 'Lasagna de carne', 'Mozzarella', 0.100
    UNION ALL SELECT 'Lasagna de carne', 'Tomate triturado', 0.150
    UNION ALL SELECT 'Lasagna de carne', 'Leche', 0.100
    UNION ALL SELECT 'Lasagna de carne', 'Manteca', 0.020
    UNION ALL SELECT 'Lasagna de carne', 'Cebolla', 0.050
    UNION ALL SELECT 'Canelones de pollo y verdura', 'Harina 000', 0.100
    UNION ALL SELECT 'Canelones de pollo y verdura', 'Huevos', 1
    UNION ALL SELECT 'Canelones de pollo y verdura', 'Pollo', 0.120
    UNION ALL SELECT 'Canelones de pollo y verdura', 'Espinaca', 0.060
    UNION ALL SELECT 'Canelones de pollo y verdura', 'Ricota', 0.060
    UNION ALL SELECT 'Canelones de pollo y verdura', 'Tomate triturado', 0.120
    UNION ALL SELECT 'Canelones de pollo y verdura', 'Leche', 0.100
    UNION ALL SELECT 'Tiramisu', 'Mascarpone', 0.080
    UNION ALL SELECT 'Tiramisu', 'Huevos', 1
    UNION ALL SELECT 'Tiramisu', 'Cafe', 0.008
    UNION ALL SELECT 'Tiramisu', 'Crema de leche', 0.030
    UNION ALL SELECT 'Flan casero con dulce de leche', 'Huevos', 2
    UNION ALL SELECT 'Flan casero con dulce de leche', 'Leche', 0.200
    UNION ALL SELECT 'Flan casero con dulce de leche', 'Dulce de leche', 0.050
    UNION ALL SELECT 'Agua mineral', 'Agua mineral', 1
    UNION ALL SELECT 'Gaseosa', 'Gaseosa', 1
    UNION ALL SELECT 'Copa de vino tinto', 'Vino tinto', 0.2
    UNION ALL SELECT 'Cerveza artesanal', 'Cerveza', 1
    UNION ALL SELECT 'Sorrentinos de calabaza y nuez', 'Harina 000', 0.120
    UNION ALL SELECT 'Sorrentinos de calabaza y nuez', 'Huevos', 1
    UNION ALL SELECT 'Sorrentinos de calabaza y nuez', 'Nueces', 0.030
    UNION ALL SELECT 'Sorrentinos de calabaza y nuez', 'Ricota', 0.080
) r
JOIN Menu  m ON m.nombre = r.menu
JOIN Stock s ON s.nombre = r.ingrediente;

-- ---------- Clientes ----------
INSERT INTO Clientes (nombre, apellido, celular, dni, email, nroRestaurante) VALUES
    ('Maria',  'Gonzalez',  '1155551001', 30111222, 'maria.gonzalez@example.com',  1),
    ('Juan',   'Perez',     '1155551002', 28222333, 'juan.perez@example.com',      1),
    ('Lucia',  'Fernandez', '2215551003', 35333444, 'lucia.fernandez@example.com', 1),
    ('Carlos', 'Rodriguez', '1155551004', 32444555, 'carlos.rodriguez@example.com',1),
    ('Sofia',  'Martinez',  '1155551005', 40555666, 'sofia.martinez@example.com',  1),
    ('Diego',  'Lopez',     '1155551006', 27666777, 'diego.lopez@example.com',     1);

-- ---------- Reservas con sena (las 4 reservas a distancia; fechas relativas a hoy) ----------
INSERT INTO Reserva (cantComensales, fechaReserva, horaReserva, estado, idCliente, idZona, nroRestaurante) VALUES
    (4, DATE_ADD(CURDATE(), INTERVAL 1 DAY),  '21:00:00', 'PENDIENTE',  1, 1, 1),   -- 1: Maria, manana, esperando confirmacion
    (2, DATE_ADD(CURDATE(), INTERVAL 1 DAY),  '20:30:00', 'CONFIRMADA', 2, 2, 1),   -- 2: Juan, manana, confirmada
    (6, DATE_ADD(CURDATE(), INTERVAL 3 DAY),  '13:30:00', 'CANCELADA',  3, 1, 1),   -- 3: Lucia, cancelada
    (3, DATE_SUB(CURDATE(), INTERVAL 1 DAY),  '21:00:00', 'CONFIRMADA', 4, 1, 1);   -- 4: Carlos, ayer, ya vino

INSERT INTO Tarjeta (titular, vencimiento) VALUES
    ('MARIA GONZALEZ',  '2029-08'),
    ('JUAN PEREZ',      '2028-11'),
    ('LUCIA FERNANDEZ', '2030-03'),
    ('CARLOS RODRIGUEZ','2029-01');

INSERT INTO Senia (monto, asistio, nroTarjeta, nroReserva) VALUES
    (30000.00, FALSE, 1, 1),
    (30000.00, FALSE, 2, 2),
    (30000.00, FALSE, 3, 3),
    (30000.00, TRUE,  4, 4);

-- ---------- Tickets ----------
INSERT INTO Ticket (nroReserva, nroRestaurante, origen) VALUES
    (4,    1, 'SALON'),      -- 1: mesa de Carlos (ayer), ya pagada en efectivo
    (NULL, 1, 'SALON'),      -- 2: mesa de hoy, todavia sin facturar
    (NULL, 1, 'DELIVERY'),   -- 3: delivery de hoy, pendiente, sin repartidor
    (NULL, 1, 'DELIVERY');   -- 4: delivery de ayer, entregado y pagado con tarjeta

-- precioUnitario = precio del menu (igual que hace la aplicacion)
INSERT INTO Pedido (idMenu, nroTicket, cantidad, precioUnitario, observaciones)
SELECT m.idMenu, x.ticket, x.cantidad, m.precio, x.obs
FROM (
    SELECT 1 AS ticket, 'Ravioles a la carbonara' AS menu, 2 AS cantidad, NULL AS obs
    UNION ALL SELECT 1, 'Lasagna de carne',              1, 'sin cebolla'
    UNION ALL SELECT 1, 'Tiramisu',                      3, NULL
    UNION ALL SELECT 1, 'Agua mineral',                  2, NULL
    UNION ALL SELECT 1, 'Copa de vino tinto',            2, NULL
    UNION ALL SELECT 2, 'Fettuccine con hongos y crema', 2, NULL
    UNION ALL SELECT 2, 'Ravioles de ricota y espinaca con fileto', 1, 'salsa aparte'
    UNION ALL SELECT 2, 'Gaseosa',                       2, NULL
    UNION ALL SELECT 3, 'Sorrentinos de jamon y queso con crema', 2, NULL
    UNION ALL SELECT 3, 'Noquis de papa con bolognesa',  1, NULL
    UNION ALL SELECT 3, 'Cerveza artesanal',             2, NULL
    UNION ALL SELECT 4, 'Lasagna de carne',              2, NULL
    UNION ALL SELECT 4, 'Flan casero con dulce de leche',2, 'con crema'
) x JOIN Menu m ON m.nombre = x.menu;

-- ---------- Facturaciones ya pagadas (ayer). El total sale de los pedidos del ticket. ----------
INSERT INTO Facturacion (nroTicket, metodoPago, montoTotal, fecha, propina, porcentajePropina, estadoPago, fechaPago)
SELECT t.nroTicket, 'EFECTIVO',
       SUM(p.cantidad * p.precioUnitario),
       DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '22:50' HOUR_MINUTE,
       ROUND(SUM(p.cantidad * p.precioUnitario) * 0.10, 2), 10, 'PAGADO',
       DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '23:10' HOUR_MINUTE
FROM Ticket t JOIN Pedido p ON p.nroTicket = t.nroTicket
WHERE t.nroTicket = 1 GROUP BY t.nroTicket;

INSERT INTO Facturacion (nroTicket, metodoPago, montoTotal, fecha, propina, porcentajePropina, estadoPago, fechaPago)
SELECT t.nroTicket, 'TARJETA',
       SUM(p.cantidad * p.precioUnitario),
       DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '21:10' HOUR_MINUTE,
       0, NULL, 'PAGADO',
       DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '21:15' HOUR_MINUTE
FROM Ticket t JOIN Pedido p ON p.nroTicket = t.nroTicket
WHERE t.nroTicket = 4 GROUP BY t.nroTicket;

-- ---------- Envios ----------
INSERT INTO Envio (direccionEntrega, nombreReceptor, estado, idCliente, nroTicket, legajoRepartidor) VALUES
    ('Av. Santa Fe 2450, 5B, CABA', 'Sofia Martinez', 'PENDIENTE', 5, 3, NULL),
    ('Belgrano 890, Quilmes',       'Diego Lopez',    'ENTREGADO', 6, 4, (SELECT legajo FROM Empleados WHERE username = 'repartidor'));
