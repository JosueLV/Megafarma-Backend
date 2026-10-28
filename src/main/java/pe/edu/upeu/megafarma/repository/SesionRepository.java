package pe.edu.upeu.megafarma.repository;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.megafarma.entity.Sesion;

public interface SesionRepository extends JpaRepository<Sesion, String> {
  @EntityGraph(attributePaths = "usuario")
  Optional<Sesion> findById(String id);

  @Modifying
  @Query("update Sesion s set s.revocada=true where s.id=:id")
  void revocar(@Param("id") String id);

  void deleteByExpiraEnBefore(Instant fecha);
}
