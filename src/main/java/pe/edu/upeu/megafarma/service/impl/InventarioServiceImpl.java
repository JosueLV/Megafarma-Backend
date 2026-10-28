package pe.edu.upeu.megafarma.service.impl;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.megafarma.dto.*;
import pe.edu.upeu.megafarma.entity.*;
import pe.edu.upeu.megafarma.exception.*;
import pe.edu.upeu.megafarma.mapper.InventarioMapper;
import pe.edu.upeu.megafarma.repository.*;
import pe.edu.upeu.megafarma.service.service.InventarioService;
import pe.edu.upeu.megafarma.util.PaginacionUtil;

@Service
@Transactional(
    readOnly = true,
    isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
public class InventarioServiceImpl implements InventarioService {
  private final ProductoRepository productos;
  private final LoteRepository lotes;
  private final InventarioMapper mapper;
  private final Clock clock;

  public InventarioServiceImpl(
      ProductoRepository productos, LoteRepository lotes, InventarioMapper mapper, Clock clock) {
    this.productos = productos;
    this.lotes = lotes;
    this.mapper = mapper;
    this.clock = clock;
  }

  public StockResponseDTO stock(Long id) {
    Producto producto =
        productos
            .findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
    return resumen(producto, totales(List.of(producto)).get(id));
  }

  private Map<Long, LoteRepository.Totales> totales(List<Producto> productos) {
    if (productos.isEmpty()) return Map.of();
    return lotes
        .totales(productos.stream().map(Producto::getId).toList(), LocalDate.now(clock))
        .stream()
        .collect(Collectors.toMap(LoteRepository.Totales::getProductoId, t -> t));
  }

  private StockResponseDTO resumen(Producto p, LoteRepository.Totales t) {
    long fisico = t == null ? 0 : t.getFisico();
    long vendible = p.isActivo() && t != null ? t.getVigente() : 0;
    return new StockResponseDTO(
        p.getId(),
        p.getCodigo(),
        p.getNombre(),
        fisico,
        vendible,
        fisico - vendible,
        p.getStockMinimo(),
        vendible <= p.getStockMinimo());
  }

  public PaginaResponseDTO<StockResponseDTO> listarStock(String buscar, int pagina, int tamanio) {
    Specification<Producto> spec = (r, q, cb) -> cb.conjunction();
    if (buscar != null && !buscar.isBlank())
      spec =
          spec.and(
              (r, q, cb) ->
                  cb.or(
                      cb.like(cb.lower(r.get("codigo")), PaginacionUtil.contiene(buscar), '\\'),
                      cb.like(cb.lower(r.get("nombre")), PaginacionUtil.contiene(buscar), '\\')));
    var paginaProductos =
        productos.findAll(
            spec, PaginacionUtil.construir(pagina, tamanio, "id", "asc", Set.of("id")));
    var resumenes = totales(paginaProductos.getContent());
    return PaginaResponseDTO.de(paginaProductos.map(p -> resumen(p, resumenes.get(p.getId()))));
  }

  public PaginaResponseDTO<LoteResponseDTO> lotes(Long productoId, int pagina, int tamanio) {
    if (productoId != null && !productos.existsById(productoId))
      throw new RecursoNoEncontradoException("Producto no encontrado");
    Specification<Lote> spec =
        (r, q, cb) ->
            productoId == null
                ? cb.conjunction()
                : cb.equal(r.get("producto").get("id"), productoId);
    return PaginaResponseDTO.de(
        lotes
            .findAll(
                spec,
                PaginacionUtil.construir(
                    pagina, tamanio, "fechaVencimiento", "asc", Set.of("fechaVencimiento")))
            .map(mapper::lote));
  }

  public LoteResponseDTO lote(Long id) {
    return mapper.lote(
        lotes
            .findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Lote no encontrado")));
  }
}
