package pe.edu.upeu.megafarma.repository;

import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.megafarma.entity.Lote;

public interface LoteRepository extends JpaRepository<Lote, Long>, JpaSpecificationExecutor<Lote> {
  Optional<Lote> findByProductoIdAndCodigo(Long productoId, String codigo);

  interface Totales {
    Long getProductoId();

    long getFisico();

    long getVigente();
  }

  @Query(
      "select l.producto.id as productoId, sum(l.cantidadDisponible) as fisico, sum(case when"
          + " l.fechaVencimiento > :hoy then l.cantidadDisponible else 0 end) as vigente from Lote"
          + " l where l.producto.id in :ids group by l.producto.id")
  List<Totales> totales(@Param("ids") Collection<Long> ids, @Param("hoy") LocalDate hoy);

  @Override
  @EntityGraph(attributePaths = "producto")
  org.springframework.data.domain.Page<Lote> findAll(
      org.springframework.data.jpa.domain.Specification<Lote> spec,
      org.springframework.data.domain.Pageable pageable);
}
