package pe.edu.upeu.megafarma.service.impl;

import java.time.*;
import java.util.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.megafarma.dto.auth.*;
import pe.edu.upeu.megafarma.entity.Sesion;
import pe.edu.upeu.megafarma.repository.*;
import pe.edu.upeu.megafarma.security.*;
import pe.edu.upeu.megafarma.service.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {
  private final UsuarioRepository usuarios;
  private final SesionRepository sesiones;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  private final Clock clock;
  private final String hashFicticio;

  public AuthServiceImpl(
      UsuarioRepository usuarios,
      SesionRepository sesiones,
      PasswordEncoder encoder,
      JwtService jwt,
      Clock clock) {
    this.usuarios = usuarios;
    this.sesiones = sesiones;
    this.encoder = encoder;
    this.jwt = jwt;
    this.clock = clock;
    this.hashFicticio = encoder.encode(UUID.randomUUID().toString());
  }

  @Transactional
  public LoginResponseDTO login(LoginRequestDTO request) {
    var optional = usuarios.findByUsername(request.username().trim().toLowerCase(Locale.ROOT));
    boolean valida =
        encoder.matches(
            request.password(), optional.map(u -> u.getPasswordHash()).orElse(hashFicticio));
    if (optional.isEmpty() || !valida || !optional.get().isActivo())
      throw new BadCredentialsException("Credenciales inválidas");
    var u = optional.get();
    var s = new Sesion();
    s.setId(UUID.randomUUID().toString());
    s.setUsuario(u);
    s.setExpiraEn(clock.instant().plusSeconds(jwt.segundos()));
    sesiones.save(s);
    var principal =
        new UsuarioPrincipal(
            u.getId(), u.getUsername(), u.getNombreCompleto(), u.getRol(), s.getId());
    return new LoginResponseDTO(
        jwt.generar(principal, s.getExpiraEn()),
        "Bearer",
        jwt.segundos(),
        u.getUsername(),
        u.getNombreCompleto(),
        List.of(u.getRol().name()),
        PermisosPorRol.nombres(u.getRol()));
  }

  public UsuarioResponseDTO me(UsuarioPrincipal p) {
    return new UsuarioResponseDTO(
        p.id(),
        p.username(),
        p.nombreCompleto(),
        List.of(p.rol().name()),
        PermisosPorRol.nombres(p.rol()));
  }

  @Transactional
  public void logout(UsuarioPrincipal p) {
    sesiones.revocar(p.sesionId());
  }
}
