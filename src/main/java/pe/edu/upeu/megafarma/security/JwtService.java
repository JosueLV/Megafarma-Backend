package pe.edu.upeu.megafarma.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey clave;
  private final Clock clock;
  private final long segundos;

  public JwtService(
      @Value("${app.jwt.secret:}") String secret,
      @Value("${app.jwt.expiracion-minutos:60}") long minutos,
      Clock clock,
      Environment env) {
    this.clock = clock;
    if (minutos < 1 || minutos > 1440)
      throw new IllegalStateException("JWT: duración permitida entre 1 y 1440 minutos");
    this.segundos = minutos * 60;
    if (secret.isBlank()) {
      if (env.acceptsProfiles(org.springframework.core.env.Profiles.of("prod")))
        throw new IllegalStateException("JWT_SECRET es obligatorio en prod");
      this.clave = Jwts.SIG.HS256.key().build();
    } else this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }

  public String generar(UsuarioPrincipal p, Instant expira) {
    var authorities = new ArrayList<>(PermisosPorRol.nombres(p.rol()));
    authorities.add("ROLE_" + p.rol().name());
    return Jwts.builder()
        .issuer("megafarma")
        .subject(p.username())
        .id(p.sesionId())
        .claim("authorities", authorities)
        .issuedAt(Date.from(clock.instant()))
        .expiration(Date.from(expira))
        .signWith(clave)
        .compact();
  }

  public Claims leer(String token) {
    return Jwts.parser()
        .verifyWith(clave)
        .requireIssuer("megafarma")
        .clock(() -> Date.from(clock.instant()))
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public long segundos() {
    return segundos;
  }
}
