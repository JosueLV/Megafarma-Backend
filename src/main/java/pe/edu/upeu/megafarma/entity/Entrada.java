package pe.edu.upeu.megafarma.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "entradas")
@Getter
@Setter
@NoArgsConstructor
public class Entrada {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 36)
  private String claveIdempotencia;

  @Column(nullable = false, length = 64)
  private String huella;

  @Column(length = 120)
  private String referencia;

  @Column(length = 500)
  private String observacion;

  @Column(nullable = false)
  private java.time.Instant creadoEn;

  @Column(nullable = false, length = 60)
  private String creadoPor;

  @OneToMany(mappedBy = "entrada", cascade = CascadeType.ALL)
  @OrderBy("id ASC")
  private java.util.List<MovimientoInventario> movimientos = new java.util.ArrayList<>();
}
