package pe.edu.upeu.megafarma.service.service;

import pe.edu.upeu.megafarma.dto.auth.*;
import pe.edu.upeu.megafarma.security.UsuarioPrincipal;

public interface AuthService {
  LoginResponseDTO login(LoginRequestDTO request);

  UsuarioResponseDTO me(UsuarioPrincipal principal);

  void logout(UsuarioPrincipal principal);
}
