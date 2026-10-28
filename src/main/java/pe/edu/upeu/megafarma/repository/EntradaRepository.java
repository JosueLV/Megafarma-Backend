package pe.edu.upeu.megafarma.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import pe.edu.upeu.megafarma.entity.Entrada;

public interface EntradaRepository extends JpaRepository<Entrada, Long> {
  Optional<Entrada> findByClaveIdempotencia(String clave);
}
