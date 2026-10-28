package pe.edu.upeu.megafarma.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movimientos_inventario")
@Getter
@Setter
@NoArgsConstructor
public class MovimientoInventario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "entrada_id")
  private Entrada entrada;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "lote_id")
  private Lote lote;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private pe.edu.upeu.megafarma.enums.TipoMovimiento tipo;

  @Column(nullable = false)
  private long cantidad;
}
