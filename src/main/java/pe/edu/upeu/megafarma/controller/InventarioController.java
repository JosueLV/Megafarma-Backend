package pe.edu.upeu.megafarma.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.megafarma.dto.*;
import pe.edu.upeu.megafarma.service.service.InventarioService;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAuthority('INVENTARIO_LEER')")
public class InventarioController {
  private final InventarioService service;

  public InventarioController(InventarioService service) {
    this.service = service;
  }

  @GetMapping("/inventario/stock")
  @Operation(summary = "HU-06 · Stock físico y vendible por producto")
  public PaginaResponseDTO<StockResponseDTO> listar(
      @RequestParam(required = false) @Size(max = 150) String buscar,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "20") int tamanio) {
    return service.listarStock(buscar, pagina, tamanio);
  }

  @GetMapping("/productos/{id}/stock")
  public StockResponseDTO stock(@PathVariable Long id) {
    return service.stock(id);
  }

  @GetMapping("/lotes")
  @Operation(summary = "HU-06 · Lotes ordenados por vencimiento")
  public PaginaResponseDTO<LoteResponseDTO> lotes(
      @RequestParam(required = false) Long productoId,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "20") int tamanio) {
    return service.lotes(productoId, pagina, tamanio);
  }

  @GetMapping("/lotes/{id}")
  public LoteResponseDTO lote(@PathVariable Long id) {
    return service.lote(id);
  }
}
