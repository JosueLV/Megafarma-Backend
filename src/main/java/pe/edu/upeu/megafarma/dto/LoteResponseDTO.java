package pe.edu.upeu.megafarma.dto;

import java.time.LocalDate;

public record LoteResponseDTO(
    Long id,
    Long productoId,
    String productoCodigo,
    String productoNombre,
    String codigo,
    LocalDate fechaVencimiento,
    long cantidadDisponible,
    boolean vencido,
    boolean vendible) {}
