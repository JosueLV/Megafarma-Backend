# Validación de la entrega

Fecha: 28 de octubre de 2026. Entorno de ejecución: Linux, OpenJDK 17.0.20, Maven 3.9.9 y Spring Boot 3.5.16.

## Resultado comprobado

- Compilación, pruebas y empaquetado Maven: **BUILD SUCCESS**.
- **30 pruebas de integración: 0 fallos, 0 errores, 0 omitidas**.
- Arranque real con `./mvnw spring-boot:run`: correcto.
- Arranque del JAR ejecutable: correcto.
- Flujo HTTP real: login → producto → entrada por lote → reintento → consulta de stock: correcto.
- Persistencia H2: el stock y la clave idempotente sobreviven al cierre y nuevo arranque del proceso.
- OpenAPI generado desde la aplicación: 11 rutas; login declarado público; [openapi.json](openapi.json).
- Maven Wrapper descargó y ejecutó Maven 3.9.9 correctamente.

## Casos automatizados

Ejecutar `./mvnw test` (Windows: `.\mvnw.cmd test`). Las pruebas usan H2 en memoria con las mismas migraciones Flyway y las capas reales de seguridad, servicios y repositorios; no simulan el inventario ni las transacciones.

| Prueba JUnit | Resultado |
|---|---|
| `falloEnSegundoLoteReviertePrimeroYMovimientos` | Aprobada |
| `rechazaProductoInexistenteEInactivo` | Aprobada |
| `rechazaVencimientoDistintoParaMismoLote` | Aprobada |
| `entradaPosteriorAlMismoLoteSumaSinDuplicarlo` | Aprobada |
| `rechazaCodigoDuplicadoSinDistinguirMayusculas` | Aprobada |
| `logoutRevocaSoloSesionActual` | Aprobada |
| `usuarioInactivoNoPuedeIniciarSesion` | Aprobada |
| `rechazaVencimientoHoyYPasado` | Aprobada |
| `loginEntregaContratoYPermisosDePharmaSoft` | Aprobada |
| `rechazaLoteRepetidoEnMismaSolicitud` | Aprobada |
| `ingresosConcurrentesAlMismoLoteNoPierdenStock` | Aprobada |
| `buscaPaginaOrdenaYControlaParametros` | Aprobada |
| `diferenciaStockFisicoDeVendibleCuandoVenceUnLote` | Aprobada |
| `sinTokenDevuelve401` | Aprobada |
| `swaggerPublicaContratoYHealthResponde` | Aprobada |
| `creaProductoConStockCero` | Aprobada |
| `reintentosConcurrentesSoloGeneranUnaEntrada` | Aprobada |
| `busquedaEscapaComodines` | Aprobada |
| `corsPermiteFrontendConAuthorizationEIdempotencia` | Aprobada |
| `tokenVencidoDevuelve401` | Aprobada |
| `ventasPuedeConsultarPeroNoCrear` | Aprobada |
| `credencialesIncorrectasDevuelven401` | Aprobada |
| `validaCamposDeProducto` | Aprobada |
| `tokenAlteradoDevuelve401` | Aprobada |
| `validaJsonCabeceraYDetalleNulo` | Aprobada |
| `desactivarUsuarioInvalidaTokenExistente` | Aprobada |
| `ingresoCreaLoteMovimientoYStockConsultables` | Aprobada |
| `cantidadesInvalidasNoCambianStock` | Aprobada |
| `mismaClaveConOtrosDatosDevuelve409` | Aprobada |
| `reintentoNoDuplicaYResponde200` | Aprobada |

## Límites de esta validación

- PostgreSQL está configurado, pero no se ejecutó una instancia PostgreSQL durante esta entrega. Validar migraciones y concurrencia contra la BD de destino antes de desplegar.
- No se ejecutó Windows de forma nativa; se incluyen los scripts oficiales de Maven Wrapper para Windows.
- Las pruebas concurrentes demuestran los casos de integridad especificados; no constituyen una prueba de capacidad ni garantizan una cifra de usuarios simultáneos.
- La interfaz, la prueba conjunta con el frontend y la aceptación del Product Owner siguen pendientes. No se marca el DoD de la hoja como aceptado.
