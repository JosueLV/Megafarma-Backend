package pe.edu.upeu.megafarma.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record EntradaRequestDTO(
    @Size(max = 120) String referencia,
    @Size(max = 500) String observacion,
    @NotEmpty @Size(max = 100) List<@NotNull @Valid Detalle> detalles) {
  public record Detalle(
      @NotNull @Positive Long productoId,
      @NotBlank @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9._-]{0,79}") String lote,
      @NotNull LocalDate fechaVencimiento,
      @NotNull @Min(1) @Max(1000000000) Long cantidad) {}
}
