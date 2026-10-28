package pe.edu.upeu.megafarma.security;

import pe.edu.upeu.megafarma.enums.Rol;

public record UsuarioPrincipal(
    Long id, String username, String nombreCompleto, Rol rol, String sesionId)
    implements java.security.Principal {
  @Override
  public String getName() {
    return username;
  }
}
