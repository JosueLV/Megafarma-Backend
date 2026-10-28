package pe.edu.upeu.megafarma.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PaginaResponseDTO<T>(
    List<T> contenido,
    int pagina,
    int tamanio,
    long totalElementos,
    int totalPaginas,
    boolean ultima) {
  public static <T> PaginaResponseDTO<T> de(Page<T> page) {
    return new PaginaResponseDTO<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isLast());
  }
}
