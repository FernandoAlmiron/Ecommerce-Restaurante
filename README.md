# Ecommerce Restaurante – Back-end (Etapa 1)

API REST en Java 17 y Spring Boot 4.1 para un restaurante: reservas con seña, salón, delivery, stock, facturación y cierre de caja, con acceso por rol.

## Requisitos

- JDK 17 (IntelliJ lo puede descargar)
- MySQL 8 y MySQL Workbench
- Postman

## Cómo ejecutarlo

1. **Base de datos.** En MySQL Workbench, conectado como `root`, ejecutar estos tres scripts en orden:
   1. `db/crear_usuario.sql` – crea el usuario `restaurante_user` con la clave `1234`
   2. `db/schema.sql` – crea la base `rest_reservas` y sus tablas
   3. `db/data.sql` – carga los datos de prueba
2. **Aplicación.** Abrir el proyecto en IntelliJ y ejecutar la clase `org.example.Main`.
   Está lista cuando la consola muestra `Tomcat started on port 8080`.
3. **Pruebas.** En Postman, importar los archivos de la carpeta `postman/` y seleccionar el environment **Restaurante local** (arriba a la derecha).

## Usuarios de prueba

La contraseña de cada usuario es su nombre + `123`. `db/data.sql` ya las carga; si la base ya tenía datos, alcanza con ejecutar `db/contrasenas_prueba.sql`.

| Usuario | Contraseña | Rol |
| --- | --- | --- |
| admin | admin123 | Administrador |
| recepcion | recepcion123 | Recepción |
| mozo | mozo123 | Mozo |
| barra | barra123 | Mozo |
| cocina | cocina123 | Cocina |
| caja | caja123 | Caja |
| repartidor | repartidor123 | Repartidor |
| rrhh | rrhh123 | Recursos Humanos |

## Configuración

Los datos de conexión se pueden cambiar con variables de entorno, sin tocar el código:

| Variable | Por defecto |
| --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/rest_reservas` |
| `DB_USER` | `restaurante_user` |
| `DB_PASSWORD` | `1234` |

Por ejemplo, para usar `root`: en Windows, `setx DB_USER "root"` y `setx DB_PASSWORD "claveDeRoot"`, y reiniciar IntelliJ. En Mac, cargarlas en *Run → Edit Configurations → Environment variables*.

## Errores frecuentes

| Mensaje | Solución |
| --- | --- |
| `Access denied for user 'restaurante_user'@'localhost'` | Ejecutar `db/crear_usuario.sql`, o revisar si quedó una variable `DB_PASSWORD` vieja en el sistema |
| `Unknown database 'rest_reservas'` | Ejecutar `db/schema.sql` |
| `Communications link failure` | Iniciar el servicio de MySQL |
| 401 en Postman | Seleccionar el environment *Restaurante local* |
