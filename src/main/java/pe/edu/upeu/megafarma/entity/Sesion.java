package pe.edu.upeu.megafarma.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "sesiones")
@Getter
@Setter
@NoArgsConstructor
public class Sesion {
  @Id
  @Column(length = 36)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  @Column(nullable = false)
  private Instant expiraEn;

  @Column(nullable = false)
  private boolean revocada;
}
