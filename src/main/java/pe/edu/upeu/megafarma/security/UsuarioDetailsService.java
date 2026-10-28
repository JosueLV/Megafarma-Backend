package pe.edu.upeu.megafarma.security;

import java.time.Clock;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.megafarma.repository.SesionRepository;

@Service
public class UsuarioDetailsService {
  private final SesionRepository sesiones;
  private final Clock clock;

  public UsuarioDetailsService(SesionRepository sesiones, Clock clock) {
    this.sesiones = sesiones;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public UsuarioPrincipal cargar(String sesionId, String username) {
    if (sesionId == null || username == null)
      throw new UsernameNotFoundException("Sesión inválida");
    var s =
        sesiones
            .findById(sesionId)
            .orElseThrow(() -> new UsernameNotFoundException("Sesión inválida"));
    var u = s.getUsuario();
    if (s.isRevocada()
        || !s.getExpiraEn().isAfter(clock.instant())
        || !u.isActivo()
        || !u.getUsername().equals(username))
      throw new UsernameNotFoundException("Sesión inválida");
    return new UsuarioPrincipal(
        u.getId(), u.getUsername(), u.getNombreCompleto(), u.getRol(), s.getId());
  }
}
