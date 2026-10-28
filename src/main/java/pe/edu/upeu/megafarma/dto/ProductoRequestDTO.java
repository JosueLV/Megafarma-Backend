package pe.edu.upeu.megafarma.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductoRequestDTO(
    @NotBlank
        @Pattern(
            regexp = "[A-Za-z0-9][A-Za-z0-9._-]{0,49}",
            message = "Use letras, números, punto, guion o guion bajo")
        String codigo,
    @NotBlank @Size(max = 150) String nombre,
    @Size(max = 500) String descripcion,
    @Size(max = 150) String principioActivo,
    @NotBlank @Size(max = 100) String presentacion,
    @Size(max = 150) String laboratorio,
    @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal precioVenta,
    @NotNull @Min(0) @Max(1000000000) Long stockMinimo) {}
