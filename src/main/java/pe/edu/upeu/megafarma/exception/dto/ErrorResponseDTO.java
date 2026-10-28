package pe.edu.upeu.megafarma.exception.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponseDTO(
    Instant timestamp,
    int status,
    String codigo,
    String mensaje,
    String ruta,
    Map<String, String> errores) {}
