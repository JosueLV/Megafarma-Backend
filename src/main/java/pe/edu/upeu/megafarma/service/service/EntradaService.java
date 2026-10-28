package pe.edu.upeu.megafarma.service.service;

import java.util.UUID;
import pe.edu.upeu.megafarma.dto.*;

public interface EntradaService {
  record Resultado(EntradaResponseDTO entrada, boolean repetida) {}

  Resultado registrar(UUID clave, EntradaRequestDTO request, String usuario);

  EntradaResponseDTO buscar(Long id);

  PaginaResponseDTO<EntradaResponseDTO> listar(int pagina, int tamanio);
}
