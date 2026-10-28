package pe.edu.upeu.megafarma.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.megafarma.dto.auth.*;
import pe.edu.upeu.megafarma.security.UsuarioPrincipal;
import pe.edu.upeu.megafarma.service.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @io.swagger.v3.oas.annotations.security.SecurityRequirements
  @PostMapping("/login")
  @Operation(
      summary = "HU-21 · Iniciar sesión",
      security = {})
  public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
    return service.login(request);
  }

  @PostMapping("/logout")
  @Operation(summary = "Cerrar la sesión actual e invalidar su JWT")
  public ResponseEntity<Void> logout(@AuthenticationPrincipal UsuarioPrincipal principal) {
    service.logout(principal);
    return ResponseEntity.noContent().build();
  }
}
