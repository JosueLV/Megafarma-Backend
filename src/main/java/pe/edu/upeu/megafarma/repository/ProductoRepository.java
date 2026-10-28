package pe.edu.upeu.megafarma.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.megafarma.entity.Producto;

public interface ProductoRepository
    extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {
  boolean existsByCodigo(String codigo);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Producto p where p.id=:id")
  Optional<Producto> bloquear(@Param("id") Long id);
}
