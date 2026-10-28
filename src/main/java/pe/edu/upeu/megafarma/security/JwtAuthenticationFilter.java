package pe.edu.upeu.megafarma.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwt;
  private final UsuarioDetailsService usuarios;
  private final RespuestaErrorSeguridad errores;

  public JwtAuthenticationFilter(
      JwtService jwt, UsuarioDetailsService usuarios, RespuestaErrorSeguridad errores) {
    this.jwt = jwt;
    this.usuarios = usuarios;
    this.errores = errores;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest r) {
    return r.getServletPath().equals("/api/v1/auth/login") || r.getMethod().equals("OPTIONS");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String header = req.getHeader("Authorization");
    if (header != null) {
      try {
        if (!header.startsWith("Bearer ")) throw new IllegalArgumentException();
        var claims = jwt.leer(header.substring(7));
        var principal = usuarios.cargar(claims.getId(), claims.getSubject());
        var authorities = new ArrayList<SimpleGrantedAuthority>();
        PermisosPorRol.nombres(principal.rol())
            .forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        authorities.add(new SimpleGrantedAuthority("ROLE_" + principal.rol().name()));
        SecurityContextHolder.getContext()
            .setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, authorities));
      } catch (JwtException | IllegalArgumentException | AuthenticationException e) {
        SecurityContextHolder.clearContext();
        errores.escribir(
            req, res, 401, "NO_AUTORIZADO", "Token inválido, vencido o sesión cerrada");
        return;
      }
    }
    chain.doFilter(req, res);
  }
}
