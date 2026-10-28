package pe.edu.upeu.megafarma.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductoResponseDTO(
    Long id,
    String codigo,
    String nombre,
    String descripcion,
    String principioActivo,
    String presentacion,
    String laboratorio,
    BigDecimal precioVenta,
    long stockMinimo,
    boolean activo,
    Instant creadoEn) {}
