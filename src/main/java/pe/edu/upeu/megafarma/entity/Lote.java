package pe.edu.upeu.megafarma.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lotes")
@Getter
@Setter
@NoArgsConstructor
public class Lote {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "producto_id")
  private Producto producto;

  @Column(nullable = false, length = 80)
  private String codigo;

  @Column(nullable = false)
  private java.time.LocalDate fechaVencimiento;

  @Column(nullable = false)
  private long cantidadDisponible;

  @Column(nullable = false)
  private java.time.Instant creadoEn;

  @Version private long version;
}
