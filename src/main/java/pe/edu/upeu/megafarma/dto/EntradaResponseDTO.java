package pe.edu.upeu.megafarma.dto;

import java.time.*;
import java.util.List;

public record EntradaResponseDTO(
    Long id,
    String claveIdempotencia,
    String referencia,
    String observacion,
    Instant creadoEn,
    String creadoPor,
    List<Detalle> detalles) {
  public record Detalle(
      Long movimientoId,
      Long productoId,
      Long loteId,
      String lote,
      LocalDate fechaVencimiento,
      long cantidad) {}
}
