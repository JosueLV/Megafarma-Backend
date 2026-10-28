package pe.edu.upeu.megafarma.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.megafarma.dto.*;
import pe.edu.upeu.megafarma.security.UsuarioPrincipal;
import pe.edu.upeu.megafarma.service.service.EntradaService;

@RestController
@RequestMapping("/api/v1/entradas")
public class EntradaController {
  private final EntradaService service;

  public EntradaController(EntradaService service) {
    this.service = service;
  }

  @PostMapping
  @PreAuthorize("hasAuthority('ENTRADA_CREAR')")
  @Operation(summary = "HU-05 · Registrar uno o varios lotes de forma atómica e idempotente")
  public ResponseEntity<EntradaResponseDTO> registrar(
      @Parameter(
              description =
                  "UUID nuevo por operación. Reutilizar el mismo UUID y cuerpo para reintentar.")
          @RequestHeader("Idempotency-Key")
          UUID clave,
      @Valid @RequestBody EntradaRequestDTO request,
      @AuthenticationPrincipal UsuarioPrincipal usuario) {
    var resultado = service.registrar(clave, request, usuario.username());
    return ResponseEntity.status(resultado.repetida() ? 200 : 201)
        .location(URI.create("/api/v1/entradas/" + resultado.entrada().id()))
        .header("Idempotency-Replayed", String.valueOf(resultado.repetida()))
        .body(resultado.entrada());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
  public EntradaResponseDTO buscar(@PathVariable Long id) {
    return service.buscar(id);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
  public PaginaResponseDTO<EntradaResponseDTO> listar(
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "20") int tamanio) {
    return service.listar(pagina, tamanio);
  }
}
