-- =====================================================================
--  rest_reservas  -  esquema completo (coincide con las entidades Java)
--  ATENCION: borra y vuelve a crear TODAS las tablas del proyecto.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS rest_reservas
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rest_reservas;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS CierreCaja, Envio, Facturacion, Pedido, Ticket, Senia, Tarjeta, Reserva,
                     Empleados, Clientes, MenuIngrediente, Menu, Stock, Proveedores, Categoria,
                     Zona, Sector, Restaurante;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------- Restaurante, sectores y zonas ----------
CREATE TABLE Restaurante (
    nroRestaurante  INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    direccion       VARCHAR(150),
    telefono        VARCHAR(20),
    email           VARCHAR(100),
    horarioApertura TIME NOT NULL,
    horarioCierre   TIME NOT NULL
);

CREATE TABLE Sector (
    idSector       INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(50) NOT NULL,
    activa         BOOLEAN NOT NULL DEFAULT TRUE,
    nroRestaurante INT NOT NULL,
    CONSTRAINT uq_sector UNIQUE (nroRestaurante, nombre),
    CONSTRAINT fk_sector_restaurante FOREIGN KEY (nroRestaurante) REFERENCES Restaurante (nroRestaurante)
);

CREATE TABLE Zona (
    idZona          INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(50) NOT NULL,
    capacidadMaxima INT NOT NULL,
    activa          BOOLEAN NOT NULL DEFAULT TRUE,
    nroRestaurante  INT NOT NULL,
    CONSTRAINT uq_zona UNIQUE (nroRestaurante, nombre),
    CONSTRAINT ck_zona_capacidad CHECK (capacidadMaxima > 0),
    CONSTRAINT fk_zona_restaurante FOREIGN KEY (nroRestaurante) REFERENCES Restaurante (nroRestaurante)
);

-- ---------- Inventario ----------
CREATE TABLE Categoria (
    nroCategoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(60) NOT NULL UNIQUE,
    descripcion  VARCHAR(200),
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE Proveedores (
    nroProveedor INT AUTO_INCREMENT PRIMARY KEY,
    cuit         CHAR(11) NOT NULL UNIQUE,
    razonSocial  VARCHAR(100) NOT NULL,
    telefono     VARCHAR(20),
    email        VARCHAR(100),
    nroCategoria INT NULL,
    CONSTRAINT fk_proveedor_categoria FOREIGN KEY (nroCategoria) REFERENCES Categoria (nroCategoria)
);

CREATE TABLE Stock (
    idStock           INT AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(80) NOT NULL,
    unidadMedida      VARCHAR(20) NOT NULL,
    cantidadActual    DOUBLE NOT NULL DEFAULT 0,
    stockMinimo       DOUBLE NOT NULL DEFAULT 0,
    stockMinimoActivo BOOLEAN NOT NULL DEFAULT FALSE,
    nroRestaurante    INT NOT NULL,
    nroCategoria      INT NULL,
    CONSTRAINT uq_stock UNIQUE (nroRestaurante, nombre),
    CONSTRAINT ck_stock_cantidad CHECK (cantidadActual >= 0),
    CONSTRAINT ck_stock_minimo CHECK (stockMinimo >= 0),
    CONSTRAINT fk_stock_restaurante FOREIGN KEY (nroRestaurante) REFERENCES Restaurante (nroRestaurante),
    CONSTRAINT fk_stock_categoria FOREIGN KEY (nroCategoria) REFERENCES Categoria (nroCategoria)
);

-- ---------- Menu ----------
CREATE TABLE Menu (
    idMenu         INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    descripcion    VARCHAR(255),
    precio         DECIMAL(10,2) NOT NULL,
    disponible     BOOLEAN NOT NULL DEFAULT TRUE,
    nroRestaurante INT NOT NULL,
    CONSTRAINT ck_menu_precio CHECK (precio > 0),
    CONSTRAINT fk_menu_restaurante FOREIGN KEY (nroRestaurante) REFERENCES Restaurante (nroRestaurante)
);

CREATE TABLE MenuIngrediente (
    idMenuIngrediente INT AUTO_INCREMENT PRIMARY KEY,
    idMenu            INT NOT NULL,
    idStock           INT NOT NULL,
    cantidadNecesaria DOUBLE NOT NULL,
    CONSTRAINT uq_menu_ingrediente UNIQUE (idMenu, idStock),
    CONSTRAINT ck_mi_cantidad CHECK (cantidadNecesaria > 0),
    CONSTRAINT fk_mi_menu FOREIGN KEY (idMenu) REFERENCES Menu (idMenu),
    CONSTRAINT fk_mi_stock FOREIGN KEY (idStock) REFERENCES Stock (idStock)
);

-- ---------- Personas ----------
CREATE TABLE Clientes (
    idCliente      INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(60) NOT NULL,
    apellido       VARCHAR(60) NOT NULL,
    celular        VARCHAR(20),
    dni            INT NOT NULL,
    email          VARCHAR(100),
    username       VARCHAR(50) UNIQUE,     -- cuenta opcional del cliente
    password       VARCHAR(100),
    nroRestaurante INT NOT NULL,
    INDEX idx_clientes_dni (nroRestaurante, dni),
    CONSTRAINT fk_cliente_restaurante FOREIGN KEY (nroRestaurante) REFERENCES Restaurante (nroRestaurante)
);

CREATE TABLE Empleados (
    legajo   INT AUTO_INCREMENT PRIMARY KEY,
    nombre   VARCHAR(60) NOT NULL,
    apellido VARCHAR(60) NOT NULL,
    celular  VARCHAR(20),
    dni      INT NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,          -- hash BCrypt (60 caracteres), nunca la clave en texto
    idSector INT NOT NULL,
    CONSTRAINT fk_empleado_sector FOREIGN KEY (idSector) REFERENCES Sector (idSector)
);

-- ---------- Reservas ----------
CREATE TABLE Reserva (
    nroReserva     INT AUTO_INCREMENT PRIMARY KEY,
    cantComensales INT NOT NULL,
    fechaReserva   DATE NOT NULL,
    horaReserva    TIME NOT NULL,
    estado         ENUM('PENDIENTE','CONFIRMADA','CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
    idCliente      INT NOT NULL,
    idZona         INT NOT NULL,
    nroRestaurante INT NOT NULL,
    INDEX idx_reserva_zona_fecha (idZona, fechaReserva, horaReserva),
    CONSTRAINT ck_reserva_comensales CHECK (cantComensales > 0),
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (idCliente) REFERENCES Clientes (idCliente),
    CONSTRAINT fk_reserva_zona FOREIGN KEY (idZona) REFERENCES Zona (idZona),
    CONSTRAINT fk_reserva_restaurante FOREIGN KEY (nroRestaurante) REFERENCES Restaurante (nroRestaurante)
);

CREATE TABLE Tarjeta (
    nroTarjeta  INT AUTO_INCREMENT PRIMARY KEY,
    titular     VARCHAR(100) NOT NULL,
    vencimiento CHAR(7) NOT NULL              -- formato AAAA-MM, ej: 2028-05
);

CREATE TABLE Senia (
    idSenia    INT AUTO_INCREMENT PRIMARY KEY,
    monto      DECIMAL(10,2) NOT NULL,
    asistio    BOOLEAN NOT NULL DEFAULT FALSE,
    nroTarjeta INT NOT NULL,
    nroReserva INT NOT NULL UNIQUE,           -- una reserva tiene como maximo una seña
    CONSTRAINT ck_senia_monto CHECK (monto >= 0),
    CONSTRAINT fk_senia_tarjeta FOREIGN KEY (nroTarjeta) REFERENCES Tarjeta (nroTarjeta),
    CONSTRAINT fk_senia_reserva FOREIGN KEY (nroReserva) REFERENCES Reserva (nroReserva)
);

-- ---------- Tickets, pedidos y facturacion ----------
CREATE TABLE Ticket (
    nroTicket      INT AUTO_INCREMENT PRIMARY KEY,
    nroReserva     INT NULL,
    nroRestaurante INT NOT NULL,
    origen         ENUM('SALON','DELIVERY') NOT NULL DEFAULT 'SALON',
    CONSTRAINT fk_ticket_reserva FOREIGN KEY (nroReserva) REFERENCES Reserva (nroReserva),
    CONSTRAINT fk_ticket_restaurante FOREIGN KEY (nroRestaurante) REFERENCES Restaurante (nroRestaurante)
);

CREATE TABLE Pedido (
    idPedido       INT AUTO_INCREMENT PRIMARY KEY,
    idMenu         INT NOT NULL,
    nroTicket      INT NOT NULL,
    cantidad       INT NOT NULL,
    precioUnitario DECIMAL(10,2) NOT NULL,
    observaciones  VARCHAR(255),
    CONSTRAINT ck_pedido_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_pedido_precio CHECK (precioUnitario >= 0),
    CONSTRAINT fk_pedido_menu FOREIGN KEY (idMenu) REFERENCES Menu (idMenu),
    CONSTRAINT fk_pedido_ticket FOREIGN KEY (nroTicket) REFERENCES Ticket (nroTicket)
);

CREATE TABLE Facturacion (
    nroFacturacion    INT AUTO_INCREMENT PRIMARY KEY,
    nroTicket         INT NOT NULL UNIQUE,    -- un ticket, una sola facturacion
    metodoPago        ENUM('TARJETA','EFECTIVO','QR') NOT NULL,
    montoTotal        DECIMAL(12,2) NOT NULL DEFAULT 0,
    fecha             DATETIME NOT NULL,
    propina           DECIMAL(12,2) NOT NULL DEFAULT 0,
    porcentajePropina INT NULL,
    estadoPago        ENUM('PENDIENTE','PAGADO') NOT NULL DEFAULT 'PENDIENTE',
    fechaPago         DATETIME NULL,
    INDEX idx_facturacion_fechapago (fechaPago),
    CONSTRAINT ck_fact_montos CHECK (montoTotal >= 0 AND propina >= 0),
    CONSTRAINT ck_fact_porcentaje CHECK (porcentajePropina IS NULL OR porcentajePropina IN (5, 10, 15)),
    CONSTRAINT ck_fact_pago CHECK (estadoPago = 'PENDIENTE' OR fechaPago IS NOT NULL),
    CONSTRAINT fk_fact_ticket FOREIGN KEY (nroTicket) REFERENCES Ticket (nroTicket)
);

-- ---------- Delivery ----------
CREATE TABLE Envio (
    idEnvio           INT AUTO_INCREMENT PRIMARY KEY,
    direccionEntrega  VARCHAR(200) NOT NULL,
    nombreReceptor    VARCHAR(100),
    estado            ENUM('PENDIENTE','EN_CAMINO','ENTREGADO','CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
    metodoPago        ENUM('TARJETA','EFECTIVO','QR') NULL,   -- lo elige el cliente al pedir
    idCliente         INT NOT NULL,
    nroTicket         INT NOT NULL UNIQUE,
    legajoRepartidor  INT NULL,
    CONSTRAINT fk_envio_cliente FOREIGN KEY (idCliente) REFERENCES Clientes (idCliente),
    CONSTRAINT fk_envio_ticket FOREIGN KEY (nroTicket) REFERENCES Ticket (nroTicket),
    CONSTRAINT fk_envio_repartidor FOREIGN KEY (legajoRepartidor) REFERENCES Empleados (legajo)
);

-- ---------- Caja ----------
CREATE TABLE CierreCaja (
    idCierre       INT AUTO_INCREMENT PRIMARY KEY,
    fecha          DATE NOT NULL,
    montoEsperado  DECIMAL(12,2) NOT NULL,
    montoContado   DECIMAL(12,2) NOT NULL,
    diferencia     DECIMAL(12,2) NOT NULL,
    observaciones  VARCHAR(255),
    legajoCajera   INT NOT NULL,
    nroRestaurante INT NOT NULL,
    CONSTRAINT ck_cierre_montos CHECK (montoEsperado >= 0 AND montoContado >= 0),
    CONSTRAINT fk_cierre_cajera FOREIGN KEY (legajoCajera) REFERENCES Empleados (legajo),
    CONSTRAINT fk_cierre_restaurante FOREIGN KEY (nroRestaurante) REFERENCES Restaurante (nroRestaurante)
);
