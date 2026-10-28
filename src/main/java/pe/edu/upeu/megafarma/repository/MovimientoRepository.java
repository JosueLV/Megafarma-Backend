package pe.edu.upeu.megafarma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.megafarma.entity.MovimientoInventario;

public interface MovimientoRepository extends JpaRepository<MovimientoInventario, Long> {}
