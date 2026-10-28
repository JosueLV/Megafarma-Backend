package pe.edu.upeu.megafarma.service.impl;

import java.time.Clock;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.megafarma.dto.*;
import pe.edu.upeu.megafarma.entity.Producto;
import pe.edu.upeu.megafarma.exception.*;
import pe.edu.upeu.megafarma.mapper.ProductoMapper;
import pe.edu.upeu.megafarma.repository.ProductoRepository;
import pe.edu.upeu.megafarma.service.service.ProductoService;
import pe.edu.upeu.megafarma.util.PaginacionUtil;

@Service
@Transactional(readOnly = true)
public class ProductoServiceImpl implements ProductoService {
  private final ProductoRepository repository;
  private final ProductoMapper mapper;
  private final Clock clock;

  public ProductoServiceImpl(ProductoRepository repository, ProductoMapper mapper, Clock clock) {
    this.repository = repository;
    this.mapper = mapper;
    this.clock = clock;
  }

  @Override
  @Transactional
  public ProductoResponseDTO crear(ProductoRequestDTO r, String usuario) {
    String codigo = r.codigo().toUpperCase(Locale.ROOT);
    if (repository.existsByCodigo(codigo))
      throw new ReglaNegocioException("CODIGO_DUPLICADO", "Ya existe un producto con ese código");
    Producto p = new Producto();
    p.setCodigo(codigo);
    p.setNombre(r.nombre().trim());
    p.setDescripcion(limpiar(r.descripcion()));
    p.setPrincipioActivo(limpiar(r.principioActivo()));
    p.setPresentacion(r.presentacion().trim());
    p.setLaboratorio(limpiar(r.laboratorio()));
    p.setPrecioVenta(r.precioVenta());
    p.setStockMinimo(r.stockMinimo());
    p.setCreadoEn(clock.instant());
    p.setCreadoPor(usuario);
    return mapper.toResponse(repository.saveAndFlush(p));
  }

  @Override
  public ProductoResponseDTO buscar(Long id) {
    return mapper.toResponse(
        repository
            .findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado")));
  }

  @Override
  public PaginaResponseDTO<ProductoResponseDTO> listar(
      String buscar, Boolean activo, int pagina, int tamanio, String ordenarPor, String direccion) {
    var pageable =
        PaginacionUtil.construir(
            pagina,
            tamanio,
            ordenarPor,
            direccion,
            Set.of("id", "codigo", "nombre", "precioVenta", "creadoEn"));
    Specification<Producto> spec = (r, q, cb) -> cb.conjunction();
    if (buscar != null && !buscar.isBlank())
      spec =
          spec.and(
              (r, q, cb) ->
                  cb.or(
                      cb.like(cb.lower(r.get("codigo")), PaginacionUtil.contiene(buscar), '\\'),
                      cb.like(cb.lower(r.get("nombre")), PaginacionUtil.contiene(buscar), '\\'),
                      cb.like(
                          cb.lower(r.get("principioActivo")),
                          PaginacionUtil.contiene(buscar),
                          '\\')));
    if (activo != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("activo"), activo));
    return PaginaResponseDTO.de(repository.findAll(spec, pageable).map(mapper::toResponse));
  }

  private String limpiar(String s) {
    return s == null ? null : s.trim();
  }
}
