package pe.edu.upeu.megafarma.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
public class Producto {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String codigo;

  @Column(nullable = false, length = 150)
  private String nombre;

  @Column(length = 500)
  private String descripcion;

  @Column(length = 150)
  private String principioActivo;

  @Column(nullable = false, length = 100)
  private String presentacion;

  @Column(length = 150)
  private String laboratorio;

  @Column(nullable = false, precision = 12, scale = 2)
  private java.math.BigDecimal precioVenta;

  @Column(nullable = false)
  private long stockMinimo;

  @Column(nullable = false)
  private boolean activo = true;

  @Column(nullable = false)
  private java.time.Instant creadoEn;

  @Column(nullable = false, length = 60)
  private String creadoPor;

  @Version private long version;
}
