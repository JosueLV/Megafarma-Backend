package pe.edu.upeu.megafarma.service.service;

import pe.edu.upeu.megafarma.dto.*;

public interface ProductoService {
  ProductoResponseDTO crear(ProductoRequestDTO request, String usuario);

  ProductoResponseDTO buscar(Long id);

  PaginaResponseDTO<ProductoResponseDTO> listar(
      String buscar, Boolean activo, int pagina, int tamanio, String ordenarPor, String direccion);
}
