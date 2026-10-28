# Revisión del documento y alcance implementado

Fuente: [documento de planificación](https://docs.google.com/spreadsheets/d/1S0L2MOtMr7jzEVHsnBPNng83Tf2Xjv8dNw7r-4y0Lr8/edit?gid=1223851460#gid=1223851460), descargado el 28 de octubre de 2026. Se inspeccionaron las cinco hojas: G1 - Baldeón (Sol de Shirán), G2 - Ticona (Maranatha Store), **G3 - Cueva (Megafarma)**, G4 - Gutierrez (Indumotos) y G5-Hoyos (clínica odontológica). Las demás hojas son proyectos distintos; sus historias no se trasladan a Megafarma.

La tercera hoja tiene 101 filas y 10 columnas. Filas 2–6: Farmacia Megafarma, equipo Dizil, sistema web para ventas e inventario. Filas 13–17: seis épicas y 24 historias. Filas 21–26: **Sprint 1**.

## Trazabilidad

| Historia | Evidencia en G3 - Cueva | Implementación backend |
|---|---|---|
| HU-21 · Iniciar sesión | C22, H14 | Login JWT/BCrypt, permisos, consulta del usuario y cierre de sesión |
| HU-01 · Registrar producto | C23, C14, B75:F85 | Registro con código único, validación, migración, dato demo y pruebas de duplicados |
| HU-03 · Buscar producto | C24, C16 | Listado paginado, búsqueda por código/nombre/principio activo y detalle |
| HU-05 · Registrar entradas por lote | C25, D14, B35:E49, B89:F101 | Producto existente, lote, vencimiento futuro, cantidad positiva, movimientos, atomicidad e idempotencia |
| HU-06 · Consultar stock por producto y lote | C26, D15 | Stock agregado por producto y consulta paginada de lotes y entradas |

HU-05 cumple técnicamente los criterios de las filas 39–45: asociación del producto, datos del lote, rechazo de cantidades inválidas, guardado relacionado, actualización sin doble registro, consultas y pruebas. La interfaz, capturas, demostración y **aceptación de Product Owner** siguen pendientes; no se declara completado el DoD del equipo ni se modifica el documento.

## Decisiones de implementación por validar con el equipo

El documento señala reglas y recursos aún por confirmar (G47:J50 y G70:J73). Para disponer de un backend ejecutable se adoptaron estas reglas explícitas:

- Producto: código único sin distinguir mayúsculas, nombre, presentación, precio de venta positivo y stock mínimo no negativo; descripción, laboratorio y principio activo opcionales. El stock no se escribe desde el formulario de producto.
- Lote único por `(producto, código)` sin distinguir mayúsculas. Una nueva recepción del mismo lote suma existencias si conserva el vencimiento. Mismo lote con otro vencimiento devuelve 409.
- La entrada acepta de 1 a 100 detalles. No admite repetir producto/lote dentro de una misma solicitud. Cantidades enteras de 1 a 1 000 000 000.
- Fecha de vencimiento estrictamente posterior a la fecha de **America/Lima**, conforme a la tarea T1 de la fila 92. Un lote cuya fecha coincide con hoy se considera no vendible. Esta convención diaria debe confirmarse antes de implementar ventas.
- Reintento: mismo `Idempotency-Key` UUID, contenido normalizado y usuario devuelve la entrada original con 200; no aumenta stock. Clave reutilizada con otros datos o usuario devuelve 409. Las claves se conservan con las entradas.
- Stock físico incluye todas las unidades almacenadas; stock vendible excluye lotes vencidos y productos inactivos. No son sinónimos.
- Roles iniciales: ADMINISTRADOR e INVENTARIO registran/consultan productos y entradas; VENTAS solo consulta en este sprint. Es una matriz inicial, no una aprobación de HU-23.
- H2 persistente para ejecución local; perfil PostgreSQL para despliegue. Java 17 y Spring Boot 3.5 mantienen la arquitectura del repositorio de referencia, que actualmente declara Java 21, Spring Boot 4.0.7 y Oracle. No es una copia byte a byte ni una modificación del repositorio del docente.

## Fuera del Sprint 1

- HU-02 y HU-04: actualización y desactivación de productos.
- HU-07 y HU-08: ajustes y alertas operativas de stock mínimo. El campo mínimo y el indicador de consulta son una base, no un motor de alertas.
- HU-09–12: registrar/consultar/buscar/anular ventas. La ficha DoD de HU-09 en filas 58–73 está en el documento, pero **HU-09 no figura en la selección del Sprint 1**.
- HU-13–16: consultas y alertas de vencimiento, bloqueo de venta y bajas. Mostrar un lote vencido y stock vendible no equivale a implementar estas historias completas.
- HU-17–20: reportes y exportaciones.
- HU-22–24: administración de cuentas y roles. El bootstrap y los roles iniciales soportan HU-21, sin presentar un CRUD de usuarios como terminado.
- Formularios, diseño responsivo y conexión visual al frontend: entrega posterior solicitada por el usuario.

No se generan commits ni se publica en el repositorio de referencia. Se entrega el proyecto para tu repositorio.
