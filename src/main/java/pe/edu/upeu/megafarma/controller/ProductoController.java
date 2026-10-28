package pe.edu.upeu.megafarma.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.megafarma.dto.*;
import pe.edu.upeu.megafarma.security.UsuarioPrincipal;
import pe.edu.upeu.megafarma.service.service.ProductoService;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {
  private final ProductoService service;

  public ProductoController(ProductoService service) {
    this.service = service;
  }

  @PostMapping
  @PreAuthorize("hasAuthority('PRODUCTO_CREAR')")
  @Operation(summary = "HU-01 · Registrar producto sin stock inicial")
  public ResponseEntity<ProductoResponseDTO> crear(
      @Valid @RequestBody ProductoRequestDTO request,
      @AuthenticationPrincipal UsuarioPrincipal usuario) {
    var result = service.crear(request, usuario.username());
    return ResponseEntity.created(URI.create("/api/v1/productos/" + result.id())).body(result);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('PRODUCTO_LEER')")
  @Operation(summary = "HU-03 · Buscar por código, nombre o principio activo")
  public PaginaResponseDTO<ProductoResponseDTO> listar(
      @RequestParam(required = false) @Size(max = 150) String buscar,
      @RequestParam(required = false) Boolean activo,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "20") int tamanio,
      @RequestParam(defaultValue = "id") String ordenarPor,
      @RequestParam(defaultValue = "asc") String direccion) {
    return service.listar(buscar, activo, pagina, tamanio, ordenarPor, direccion);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('PRODUCTO_LEER')")
  public ProductoResponseDTO buscar(@PathVariable Long id) {
    return service.buscar(id);
  }
}
