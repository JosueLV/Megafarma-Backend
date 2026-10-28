package pe.edu.upeu.megafarma.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.megafarma.dto.auth.UsuarioResponseDTO;
import pe.edu.upeu.megafarma.security.UsuarioPrincipal;
import pe.edu.upeu.megafarma.service.service.AuthService;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {
  private final AuthService service;

  public UsuarioController(AuthService service) {
    this.service = service;
  }

  @GetMapping("/me")
  public UsuarioResponseDTO me(@AuthenticationPrincipal UsuarioPrincipal principal) {
    return service.me(principal);
  }
}
