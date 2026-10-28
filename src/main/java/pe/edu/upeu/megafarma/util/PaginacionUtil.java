package pe.edu.upeu.megafarma.util;

import java.util.Set;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import pe.edu.upeu.megafarma.exception.ReglaNegocioException;

public final class PaginacionUtil {
  private PaginacionUtil() {}

  public static Pageable construir(
      int pagina, int tamanio, String ordenarPor, String direccion, Set<String> permitidos) {
    if (pagina < 0
        || tamanio < 1
        || tamanio > 100
        || !permitidos.contains(ordenarPor)
        || !("asc".equalsIgnoreCase(direccion) || "desc".equalsIgnoreCase(direccion)))
      throw new ReglaNegocioException(
          "PAGINACION_INVALIDA",
          "pagina >= 0, tamanio entre 1 y 100, dirección asc/desc y orden permitido",
          HttpStatus.BAD_REQUEST);
    Sort sort = Sort.by(Sort.Direction.fromString(direccion), ordenarPor);
    if (!"id".equals(ordenarPor)) sort = sort.and(Sort.by("id"));
    return PageRequest.of(pagina, tamanio, sort);
  }

  public static String contiene(String texto) {
    return "%"
        + texto
            .trim()
            .toLowerCase(java.util.Locale.ROOT)
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
        + "%";
  }
}
