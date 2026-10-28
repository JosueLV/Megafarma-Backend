package pe.edu.upeu.megafarma;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import pe.edu.upeu.megafarma.repository.*;
import pe.edu.upeu.megafarma.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Sprint1IntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate jdbc;
  @Autowired LoteRepository lotes;
  @Autowired EntradaRepository entradas;
  @Autowired MovimientoRepository movimientos;
  @Autowired UsuarioRepository usuarios;
  @Autowired JwtService jwt;
  @Autowired Clock clock;
  String admin, ventas;

  @BeforeAll
  void acceder() throws Exception {
    admin = login("admin", "AdminDemo2026!");
    ventas = login("ventas", "VentasDemo2026!");
  }

  String login(String user, String password) throws Exception {
    var r =
        mvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        json.writeValueAsString(Map.of("username", user, "password", password))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("tipo").value("Bearer"))
            .andExpect(jsonPath("expiraEnSegundos").value(3600))
            .andExpect(jsonPath("password").doesNotExist())
            .andReturn();
    return body(r).get("token").asText();
  }

  JsonNode body(MvcResult result) throws Exception {
    return json.readTree(result.getResponse().getContentAsString());
  }

  Map<String, Object> producto(String codigo) {
    return Map.of(
        "codigo",
        codigo,
        "nombre",
        "Amoxicilina de prueba",
        "presentacion",
        "Cápsula",
        "precioVenta",
        2.50,
        "stockMinimo",
        5);
  }

  long crear() throws Exception {
    return body(mvc.perform(
                post("/api/v1/productos")
                    .header("Authorization", "Bearer " + admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(producto("P-" + UUID.randomUUID()))))
            .andExpect(status().isCreated())
            .andReturn())
        .get("id")
        .asLong();
  }

  Map<String, Object> detalle(long p, String lote, long cantidad, LocalDate fecha) {
    return Map.of(
        "productoId", p, "lote", lote, "cantidad", cantidad, "fechaVencimiento", fecha.toString());
  }

  Map<String, Object> entrada(long p, String lote, long cantidad) {
    return Map.of(
        "referencia",
        "GUIA-PRUEBA",
        "detalles",
        List.of(detalle(p, lote, cantidad, LocalDate.now(clock).plusYears(1))));
  }

  ResultActions ingresar(String clave, Object contenido) throws Exception {
    return mvc.perform(
        post("/api/v1/entradas")
            .header("Authorization", "Bearer " + admin)
            .header("Idempotency-Key", clave)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(contenido)));
  }

  JsonNode stock(long p) throws Exception {
    return body(
        mvc.perform(
                get("/api/v1/productos/{id}/stock", p).header("Authorization", "Bearer " + admin))
            .andExpect(status().isOk())
            .andReturn());
  }

  @Test
  void loginEntregaContratoYPermisosDePharmaSoft() throws Exception {
    mvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer " + admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("username").value("admin"))
        .andExpect(jsonPath("roles[0]").value("ADMINISTRADOR"))
        .andExpect(jsonPath("permisos").isArray());
    assertNotEquals(
        "AdminDemo2026!", usuarios.findByUsername("admin").orElseThrow().getPasswordHash());
  }

  @Test
  void credencialesIncorrectasDevuelven401() throws Exception {
    mvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"incorrecta\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("codigo").value("CREDENCIALES_INVALIDAS"));
  }

  @Test
  void sinTokenDevuelve401() throws Exception {
    mvc.perform(get("/api/v1/productos")).andExpect(status().isUnauthorized());
  }

  @Test
  void tokenAlteradoDevuelve401() throws Exception {
    mvc.perform(get("/api/v1/productos").header("Authorization", "Bearer falso.jwt.token"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void tokenVencidoDevuelve401() throws Exception {
    var p =
        new pe.edu.upeu.megafarma.security.UsuarioPrincipal(
            1L,
            "admin",
            "Admin",
            pe.edu.upeu.megafarma.enums.Rol.ADMINISTRADOR,
            UUID.randomUUID().toString());
    String vencido = jwt.generar(p, clock.instant().minusSeconds(60));
    mvc.perform(get("/api/v1/productos").header("Authorization", "Bearer " + vencido))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void usuarioInactivoNoPuedeIniciarSesion() throws Exception {
    jdbc.update("update usuarios set activo=false where username='inventario'");
    try {
      mvc.perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"username\":\"inventario\",\"password\":\"InventarioDemo2026!\"}"))
          .andExpect(status().isUnauthorized());
    } finally {
      jdbc.update("update usuarios set activo=true where username='inventario'");
    }
  }

  @Test
  void desactivarUsuarioInvalidaTokenExistente() throws Exception {
    String token = login("inventario", "InventarioDemo2026!");
    jdbc.update("update usuarios set activo=false where username='inventario'");
    try {
      mvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer " + token))
          .andExpect(status().isUnauthorized());
    } finally {
      jdbc.update("update usuarios set activo=true where username='inventario'");
    }
  }

  @Test
  void logoutRevocaSoloSesionActual() throws Exception {
    String token = login("admin", "AdminDemo2026!");
    mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
    mvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer " + admin))
        .andExpect(status().isOk());
  }

  @Test
  void ventasPuedeConsultarPeroNoCrear() throws Exception {
    mvc.perform(get("/api/v1/productos").header("Authorization", "Bearer " + ventas))
        .andExpect(status().isOk());
    mvc.perform(
            post("/api/v1/productos")
                .header("Authorization", "Bearer " + ventas)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(producto("PROHIBIDO"))))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/api/v1/entradas")
                .header("Authorization", "Bearer " + ventas)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(entrada(crear(), "A", 1))))
        .andExpect(status().isForbidden());
  }

  @Test
  void creaProductoConStockCero() throws Exception {
    long id = crear();
    assertEquals(0, stock(id).get("stockFisico").asLong());
  }

  @Test
  void rechazaCodigoDuplicadoSinDistinguirMayusculas() throws Exception {
    String code = "UNICO-" + UUID.randomUUID();
    mvc.perform(
            post("/api/v1/productos")
                .header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(producto(code))))
        .andExpect(status().isCreated());
    mvc.perform(
            post("/api/v1/productos")
                .header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(producto(code.toLowerCase(Locale.ROOT)))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("codigo").value("CODIGO_DUPLICADO"));
  }

  @Test
  void validaCamposDeProducto() throws Exception {
    Map<String, Object> p = new HashMap<>(producto("VALIDAR"));
    p.put("nombre", " ");
    p.put("precioVenta", -2);
    mvc.perform(
            post("/api/v1/productos")
                .header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(p)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("errores.nombre").exists())
        .andExpect(jsonPath("errores.precioVenta").exists());
  }

  @Test
  void buscaPaginaOrdenaYControlaParametros() throws Exception {
    long id = crear();
    mvc.perform(
            get("/api/v1/productos")
                .param("buscar", "amoxicilina")
                .param("tamanio", "2")
                .param("ordenarPor", "nombre")
                .header("Authorization", "Bearer " + admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("contenido").isArray())
        .andExpect(jsonPath("tamanio").value(2))
        .andExpect(jsonPath("totalElementos").isNumber());
    mvc.perform(
            get("/api/v1/productos")
                .param("buscar", "ZZZ-SIN-COINCIDENCIA")
                .header("Authorization", "Bearer " + admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("contenido").isEmpty());
    for (String tamanio : List.of("0", "101", "-1"))
      mvc.perform(
              get("/api/v1/productos")
                  .param("tamanio", tamanio)
                  .header("Authorization", "Bearer " + admin))
          .andExpect(status().isBadRequest());
    mvc.perform(
            get("/api/v1/productos")
                .param("ordenarPor", "passwordHash")
                .header("Authorization", "Bearer " + admin))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/productos/{id}", id).header("Authorization", "Bearer " + admin))
        .andExpect(status().isOk());
  }

  @Test
  void busquedaEscapaComodines() throws Exception {
    crear();
    mvc.perform(
            get("/api/v1/productos")
                .param("buscar", "%_%")
                .header("Authorization", "Bearer " + admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("contenido").isEmpty());
  }

  @Test
  void ingresoCreaLoteMovimientoYStockConsultables() throws Exception {
    long p = crear();
    var r =
        body(
            ingresar(UUID.randomUUID().toString(), entrada(p, "LOTE-1", 12))
                .andExpect(status().isCreated())
                .andReturn());
    assertEquals(12, stock(p).get("stockFisico").asLong());
    assertEquals(12, stock(p).get("stockVendible").asLong());
    mvc.perform(
            get("/api/v1/entradas/{id}", r.get("id").asLong())
                .header("Authorization", "Bearer " + admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("detalles[0].cantidad").value(12))
        .andExpect(jsonPath("creadoPor").value("admin"));
    mvc.perform(
            get("/api/v1/lotes")
                .param("productoId", String.valueOf(p))
                .header("Authorization", "Bearer " + admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("contenido[0].cantidadDisponible").value(12));
  }

  @Test
  void reintentoNoDuplicaYResponde200() throws Exception {
    long p = crear();
    String key = UUID.randomUUID().toString();
    var payload = entrada(p, "REINTENTO", 7);
    long id =
        body(ingresar(key, payload).andExpect(status().isCreated()).andReturn()).get("id").asLong();
    ingresar(key, payload)
        .andExpect(status().isOk())
        .andExpect(header().string("Idempotency-Replayed", "true"))
        .andExpect(jsonPath("id").value(id));
    assertEquals(7, stock(p).get("stockFisico").asLong());
  }

  @Test
  void mismaClaveConOtrosDatosDevuelve409() throws Exception {
    long p = crear();
    String key = UUID.randomUUID().toString();
    ingresar(key, entrada(p, "CLAVE", 5)).andExpect(status().isCreated());
    ingresar(key, entrada(p, "CLAVE", 6))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("codigo").value("CLAVE_REUTILIZADA"));
    assertEquals(5, stock(p).get("stockFisico").asLong());
  }

  @Test
  void entradaPosteriorAlMismoLoteSumaSinDuplicarlo() throws Exception {
    long p = crear();
    ingresar(UUID.randomUUID().toString(), entrada(p, "MISMO", 4)).andExpect(status().isCreated());
    ingresar(UUID.randomUUID().toString(), entrada(p, "mismo", 6)).andExpect(status().isCreated());
    assertEquals(10, stock(p).get("stockFisico").asLong());
    assertEquals(
        1, jdbc.queryForObject("select count(*) from lotes where producto_id=?", Integer.class, p));
  }

  @Test
  void rechazaVencimientoDistintoParaMismoLote() throws Exception {
    long p = crear();
    ingresar(UUID.randomUUID().toString(), entrada(p, "FECHA", 4)).andExpect(status().isCreated());
    ingresar(
            UUID.randomUUID().toString(),
            Map.of("detalles", List.of(detalle(p, "FECHA", 5, LocalDate.now(clock).plusYears(2)))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("codigo").value("VENCIMIENTO_LOTE_DISTINTO"));
    assertEquals(4, stock(p).get("stockFisico").asLong());
  }

  @Test
  void cantidadesInvalidasNoCambianStock() throws Exception {
    long p = crear();
    for (long n : List.of(0L, -1L, 1000000001L))
      ingresar(UUID.randomUUID().toString(), entrada(p, "INVALIDO", n))
          .andExpect(status().isBadRequest());
    assertEquals(0, stock(p).get("stockFisico").asLong());
  }

  @Test
  void rechazaVencimientoHoyYPasado() throws Exception {
    long p = crear();
    for (int dias : List.of(0, -1))
      ingresar(
              UUID.randomUUID().toString(),
              Map.of(
                  "detalles",
                  List.of(detalle(p, "VENCIDO", 1, LocalDate.now(clock).plusDays(dias)))))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("codigo").value("VENCIMIENTO_INVALIDO"));
  }

  @Test
  void falloEnSegundoLoteReviertePrimeroYMovimientos() throws Exception {
    long p = crear();
    long antes = entradas.count();
    long movAntes = movimientos.count();
    ingresar(
            UUID.randomUUID().toString(),
            Map.of(
                "detalles",
                List.of(
                    detalle(p, "A-VALIDO", 8, LocalDate.now(clock).plusYears(1)),
                    detalle(p, "Z-INVALIDO", 4, LocalDate.now(clock)))))
        .andExpect(status().isBadRequest());
    assertEquals(0, stock(p).get("stockFisico").asLong());
    assertEquals(antes, entradas.count());
    assertEquals(movAntes, movimientos.count());
    assertEquals(
        0, jdbc.queryForObject("select count(*) from lotes where producto_id=?", Integer.class, p));
  }

  @Test
  void rechazaProductoInexistenteEInactivo() throws Exception {
    ingresar(UUID.randomUUID().toString(), entrada(Long.MAX_VALUE, "NO-EXISTE", 1))
        .andExpect(status().isNotFound());
    long p = crear();
    jdbc.update("update productos set activo=false where id=?", p);
    ingresar(UUID.randomUUID().toString(), entrada(p, "INACTIVO", 1))
        .andExpect(status().isConflict());
  }

  @Test
  void rechazaLoteRepetidoEnMismaSolicitud() throws Exception {
    long p = crear();
    var d = detalle(p, "REPETIDO", 2, LocalDate.now(clock).plusYears(1));
    ingresar(UUID.randomUUID().toString(), Map.of("detalles", List.of(d, d)))
        .andExpect(status().isConflict());
    assertEquals(0, stock(p).get("stockFisico").asLong());
  }

  @Test
  void validaJsonCabeceraYDetalleNulo() throws Exception {
    mvc.perform(
            post("/api/v1/entradas")
                .header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest());
    ingresar("no-es-uuid", entrada(crear(), "X", 1)).andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/v1/entradas")
                .header("Authorization", "Bearer " + admin)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"detalles\":[null]}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void diferenciaStockFisicoDeVendibleCuandoVenceUnLote() throws Exception {
    long p = crear();
    ingresar(UUID.randomUUID().toString(), entrada(p, "VENCE", 5)).andExpect(status().isCreated());
    jdbc.update(
        "update lotes set fecha_vencimiento=? where producto_id=?",
        java.sql.Date.valueOf(LocalDate.now(clock)),
        p);
    assertEquals(5, stock(p).get("stockFisico").asLong());
    assertEquals(0, stock(p).get("stockVendible").asLong());
    assertEquals(5, stock(p).get("stockNoVendible").asLong());
  }

  @Test
  void ingresosConcurrentesAlMismoLoteNoPierdenStock() throws Exception {
    long p = crear();
    List<Integer> estados =
        concurrente(
            () ->
                ingresar(UUID.randomUUID().toString(), entrada(p, "CONCURRENTE", 5))
                    .andReturn()
                    .getResponse()
                    .getStatus(),
            4);
    assertEquals(List.of(201, 201, 201, 201), estados);
    assertEquals(20, stock(p).get("stockFisico").asLong());
    assertEquals(
        1, jdbc.queryForObject("select count(*) from lotes where producto_id=?", Integer.class, p));
  }

  @Test
  void reintentosConcurrentesSoloGeneranUnaEntrada() throws Exception {
    long p = crear();
    String key = UUID.randomUUID().toString();
    List<Integer> estados =
        concurrente(
            () -> ingresar(key, entrada(p, "IDEMPOTENTE", 9)).andReturn().getResponse().getStatus(),
            4);
    assertEquals(1, estados.stream().filter(s -> s == 201).count());
    assertEquals(3, estados.stream().filter(s -> s == 200).count());
    assertEquals(9, stock(p).get("stockFisico").asLong());
  }

  List<Integer> concurrente(Callable<Integer> tarea, int n) throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(n);
    CountDownLatch inicio = new CountDownLatch(1);
    try {
      List<Future<Integer>> futures = new ArrayList<>();
      for (int i = 0; i < n; i++)
        futures.add(
            pool.submit(
                () -> {
                  inicio.await();
                  return tarea.call();
                }));
      inicio.countDown();
      List<Integer> resultados = new ArrayList<>();
      for (var f : futures) resultados.add(f.get(30, TimeUnit.SECONDS));
      return resultados;
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void corsPermiteFrontendConAuthorizationEIdempotencia() throws Exception {
    mvc.perform(
            options("/api/v1/entradas")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST")
                .header(
                    "Access-Control-Request-Headers", "authorization,content-type,idempotency-key"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    mvc.perform(
            options("/api/v1/entradas")
                .header("Origin", "https://origen-no-autorizado.example")
                .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isForbidden());
  }

  @Test
  void swaggerPublicaContratoYHealthResponde() throws Exception {
    mvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("paths['/api/v1/auth/login']").exists())
        .andExpect(jsonPath("paths['/api/v1/auth/login'].post.security").isEmpty())
        .andExpect(jsonPath("paths['/api/v1/entradas']").exists());
    mvc.perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("status").value("UP"));
  }
}
