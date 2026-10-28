package pe.edu.upeu.megafarma.service.service;

import pe.edu.upeu.megafarma.dto.*;

public interface InventarioService {
  StockResponseDTO stock(Long productoId);

  PaginaResponseDTO<StockResponseDTO> listarStock(String buscar, int pagina, int tamanio);

  PaginaResponseDTO<LoteResponseDTO> lotes(Long productoId, int pagina, int tamanio);

  LoteResponseDTO lote(Long id);
}
