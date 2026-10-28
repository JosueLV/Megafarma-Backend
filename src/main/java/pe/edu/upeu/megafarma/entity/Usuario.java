package pe.edu.upeu.megafarma.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 60)
  private String username;

  @Column(nullable = false, length = 100)
  private String passwordHash;

  @Column(nullable = false, length = 150)
  private String nombreCompleto;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private pe.edu.upeu.megafarma.enums.Rol rol;

  @Column(nullable = false)
  private boolean activo = true;
}
