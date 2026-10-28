# Contrato de integración

Base local `http://localhost:8080/api/v1`. Todos los cuerpos son JSON. Guarda el token de forma acorde con el diseño de seguridad del frontend; no lo incluyas en URL ni logs. Estas funciones ilustran la integración y no son una aplicación frontend terminada.

```javascript
const API = 'http://localhost:8080/api/v1';
let token;
async function request(path, {method='GET', body, headers={}}={}) {
  const response = await fetch(API+path, {
    method,
    headers: {
      ...(body ? {'Content-Type':'application/json'} : {}),
      ...(token ? {Authorization:`Bearer ${token}`} : {}),
      ...headers
    },
    ...(body ? {body:JSON.stringify(body)} : {})
  });
  const data = response.status === 204 ? null : await response.json();
  if (!response.ok) {
    if (response.status === 401) token = undefined;
    throw {status:response.status, ...data};
  }
  return data;
}
async function login(username,password) {
  const sesion = await request('/auth/login',{method:'POST',body:{username,password}});
  token=sesion.token;
  return sesion; // roles y permisos para mostrar las acciones pertinentes
}
// Genera la clave al comenzar el guardado y consérvala hasta obtener confirmación.
// Si hay un error de red, vuelve a enviar exactamente este cuerpo y esta clave.
const claveEntrada = crypto.randomUUID();
const payload = {detalles:[{productoId:1,lote:'ABC',fechaVencimiento:'2030-12-31',cantidad:10}]};
// await request('/entradas',{method:'POST',body:payload,headers:{'Idempotency-Key':claveEntrada}});
```

| Operación | Método y ruta | Permiso |
|---|---|---|
| Login | POST /auth/login | Público |
| Usuario actual | GET /usuarios/me | Sesión válida |
| Logout | POST /auth/logout | Sesión válida |
| Crear producto | POST /productos | PRODUCTO_CREAR |
| Buscar/listar productos | GET /productos | PRODUCTO_LEER |
| Detalle producto | GET /productos/{id} | PRODUCTO_LEER |
| Registrar entrada | POST /entradas | ENTRADA_CREAR |
| Listar/detalle entradas | GET /entradas y /entradas/{id} | INVENTARIO_LEER |
| Consultar stock | GET /inventario/stock y /productos/{id}/stock | INVENTARIO_LEER |
| Consultar lotes | GET /lotes y /lotes/{id} | INVENTARIO_LEER |

Paginación: `pagina` desde 0 y `tamanio` entre 1 y 100, por defecto 20. La respuesta contiene `contenido`, `pagina`, `tamanio`, `totalElementos`, `totalPaginas`, `ultima`. Sin resultados: 200 con `contenido: []`.

Productos: `buscar` consulta parcialmente código, nombre y principio activo; `activo` es opcional. `ordenarPor`: id, codigo, nombre, precioVenta o creadoEn; `direccion`: asc/desc. Lotes: filtro `productoId`, orden por vencimiento e id. Entradas: orden descendente por id.

Nueva entrada: 201 y `Location`; reintento reconocido: 200 y `Idempotency-Replayed: true`. El contenido devuelve cantidades recibidas, no un saldo que podría cambiar por operaciones posteriores. Consulta el stock al finalizar.

Errores:

```json
{
  "timestamp": "2026-10-28T20:00:00Z",
  "status": 400,
  "codigo": "VALIDACION",
  "mensaje": "Revisa los campos indicados",
  "ruta": "/api/v1/productos",
  "errores": {"nombre":"no debe estar vacío"}
}
```

- 400: datos/JSON/fechas/cabeceras/paginación inválidos; mostrar `errores` junto a los campos.
- 401: credenciales, token o sesión inválidos; limpiar sesión local y solicitar login.
- 403: sesión válida, operación no autorizada.
- 404: recurso inexistente.
- 409: duplicado, conflicto de lote, reutilización de clave o concurrencia. No crear una clave nueva para un reintento de red; corregir el conflicto según `codigo`.
- 500: fallo inesperado; no se devuelve SQL ni stacktrace.

Aún no existen endpoints de ventas, reportes o administración de cuentas; corresponden a los siguientes sprints. Swagger es la especificación ejecutable de las rutas implementadas.
