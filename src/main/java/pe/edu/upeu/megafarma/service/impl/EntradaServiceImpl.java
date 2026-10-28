package pe.edu.upeu.megafarma.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import pe.edu.upeu.megafarma.dto.*;
import pe.edu.upeu.megafarma.entity.*;
import pe.edu.upeu.megafarma.enums.TipoMovimiento;
import pe.edu.upeu.megafarma.exception.*;
import pe.edu.upeu.megafarma.mapper.InventarioMapper;
import pe.edu.upeu.megafarma.repository.*;
import pe.edu.upeu.megafarma.service.service.EntradaService;
import pe.edu.upeu.megafarma.util.PaginacionUtil;

@Service
public class EntradaServiceImpl implements EntradaService {
  private final ProductoRepository productos;
  private final LoteRepository lotes;
  private final EntradaRepository entradas;
  private final InventarioMapper mapper;
  private final ObjectMapper json;
  private final Clock clock;
  private final TransactionTemplate tx;

  public EntradaServiceImpl(
      ProductoRepository productos,
      LoteRepository lotes,
      EntradaRepository entradas,
      InventarioMapper mapper,
      ObjectMapper json,
      Clock clock,
      PlatformTransactionManager tm) {
    this.productos = productos;
    this.lotes = lotes;
    this.entradas = entradas;
    this.mapper = mapper;
    this.json = json;
    this.clock = clock;
    this.tx = new TransactionTemplate(tm);
  }

  public Resultado registrar(UUID clave, EntradaRequestDTO original, String usuario) {
    EntradaRequestDTO r = normalizar(original);
    String huella = huella(r, usuario);
    // El catch está FUERA de la transacción: una violación UNIQUE invalida la transacción actual.
    try {
      return tx.execute(s -> registrarTransaccional(clave.toString(), r, huella, usuario));
    } catch (DataIntegrityViolationException ex) {
      Resultado repetida =
          tx.execute(
              s ->
                  entradas
                      .findByClaveIdempotencia(clave.toString())
                      .map(e -> repetir(e, huella))
                      .orElse(null));
      if (repetida != null) return repetida;
      throw ex;
    }
  }

  private Resultado registrarTransaccional(
      String clave, EntradaRequestDTO r, String huella, String usuario) {
    var anterior = entradas.findByClaveIdempotencia(clave);
    if (anterior.isPresent()) return repetir(anterior.get(), huella);
    Set<String> claves = new HashSet<>();
    for (var d : r.detalles()) {
      if (!claves.add(d.productoId() + ":" + d.lote()))
        throw new ReglaNegocioException(
            "LOTE_REPETIDO", "Un lote solo puede aparecer una vez por entrada");
    }
    // Bloquear siempre productos en orden ascendente evita sobreescrituras y ciclos de bloqueo.
    Map<Long, Producto> bloqueados = new HashMap<>();
    r.detalles().stream()
        .map(EntradaRequestDTO.Detalle::productoId)
        .distinct()
        .sorted()
        .forEach(
            id ->
                bloqueados.put(
                    id,
                    productos
                        .bloquear(id)
                        .orElseThrow(
                            () ->
                                new RecursoNoEncontradoException(
                                    "Producto no encontrado: " + id))));
    anterior = entradas.findByClaveIdempotencia(clave);
    if (anterior.isPresent()) return repetir(anterior.get(), huella);
    Entrada entrada = new Entrada();
    entrada.setClaveIdempotencia(clave);
    entrada.setHuella(huella);
    entrada.setReferencia(r.referencia());
    entrada.setObservacion(r.observacion());
    entrada.setCreadoEn(clock.instant());
    entrada.setCreadoPor(usuario);
    for (var d : r.detalles()) {
      Producto p = bloqueados.get(d.productoId());
      if (!p.isActivo())
        throw new ReglaNegocioException(
            "PRODUCTO_INACTIVO", "No se puede ingresar mercadería de un producto inactivo");
      if (!d.fechaVencimiento().isAfter(LocalDate.now(clock)))
        throw new ReglaNegocioException(
            "VENCIMIENTO_INVALIDO",
            "La fecha de vencimiento debe ser posterior a hoy",
            org.springframework.http.HttpStatus.BAD_REQUEST);
      Lote lote =
          lotes
              .findByProductoIdAndCodigo(p.getId(), d.lote())
              .orElseGet(
                  () -> {
                    Lote nuevo = new Lote();
                    nuevo.setProducto(p);
                    nuevo.setCodigo(d.lote());
                    nuevo.setFechaVencimiento(d.fechaVencimiento());
                    nuevo.setCreadoEn(clock.instant());
                    return nuevo;
                  });
      if (!lote.getFechaVencimiento().equals(d.fechaVencimiento()))
        throw new ReglaNegocioException(
            "VENCIMIENTO_LOTE_DISTINTO", "El lote ya existe con otra fecha de vencimiento");
      try {
        lote.setCantidadDisponible(Math.addExact(lote.getCantidadDisponible(), d.cantidad()));
      } catch (ArithmeticException e) {
        throw new ReglaNegocioException("STOCK_FUERA_RANGO", "El stock supera el límite permitido");
      }
      lote = lotes.save(lote);
      MovimientoInventario movimiento = new MovimientoInventario();
      movimiento.setEntrada(entrada);
      movimiento.setLote(lote);
      movimiento.setTipo(TipoMovimiento.ENTRADA);
      movimiento.setCantidad(d.cantidad());
      entrada.getMovimientos().add(movimiento);
    }
    entradas.saveAndFlush(entrada);
    return new Resultado(mapper.entrada(entrada), false);
  }

  private Resultado repetir(Entrada e, String huella) {
    if (!e.getHuella().equals(huella))
      throw new ReglaNegocioException(
          "CLAVE_REUTILIZADA", "Idempotency-Key ya se utilizó con otros datos o usuario");
    return new Resultado(mapper.entrada(e), true);
  }

  private EntradaRequestDTO normalizar(EntradaRequestDTO r) {
    return new EntradaRequestDTO(
        limpiar(r.referencia()),
        limpiar(r.observacion()),
        r.detalles().stream()
            .map(
                d ->
                    new EntradaRequestDTO.Detalle(
                        d.productoId(),
                        d.lote().toUpperCase(Locale.ROOT),
                        d.fechaVencimiento(),
                        d.cantidad()))
            .sorted(
                Comparator.comparing(EntradaRequestDTO.Detalle::productoId)
                    .thenComparing(EntradaRequestDTO.Detalle::lote))
            .toList());
  }

  private String limpiar(String s) {
    return s == null ? null : s.trim();
  }

  private String huella(EntradaRequestDTO r, String usuario) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256")
                  .digest(
                      (usuario + "\n" + json.writeValueAsString(r))
                          .getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException("No se pudo identificar la operación", e);
    }
  }

  @Transactional(readOnly = true)
  public EntradaResponseDTO buscar(Long id) {
    return mapper.entrada(
        entradas
            .findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Entrada no encontrada")));
  }

  @Transactional(readOnly = true)
  public PaginaResponseDTO<EntradaResponseDTO> listar(int pagina, int tamanio) {
    return PaginaResponseDTO.de(
        entradas
            .findAll(PaginacionUtil.construir(pagina, tamanio, "id", "desc", Set.of("id")))
            .map(mapper::entrada));
  }
}
