package pe.edu.upeu.megafarma.mapper;

import java.time.*;
import org.springframework.stereotype.Component;
import pe.edu.upeu.megafarma.dto.*;
import pe.edu.upeu.megafarma.entity.*;

@Component
public class InventarioMapper {
  private final Clock clock;

  public InventarioMapper(Clock clock) {
    this.clock = clock;
  }

  public LoteResponseDTO lote(Lote l) {
    boolean vencido = !l.getFechaVencimiento().isAfter(LocalDate.now(clock));
    Producto p = l.getProducto();
    return new LoteResponseDTO(
        l.getId(),
        p.getId(),
        p.getCodigo(),
        p.getNombre(),
        l.getCodigo(),
        l.getFechaVencimiento(),
        l.getCantidadDisponible(),
        vencido,
        !vencido && p.isActivo() && l.getCantidadDisponible() > 0);
  }

  public EntradaResponseDTO entrada(Entrada e) {
    return new EntradaResponseDTO(
        e.getId(),
        e.getClaveIdempotencia(),
        e.getReferencia(),
        e.getObservacion(),
        e.getCreadoEn(),
        e.getCreadoPor(),
        e.getMovimientos().stream()
            .map(
                m ->
                    new EntradaResponseDTO.Detalle(
                        m.getId(),
                        m.getLote().getProducto().getId(),
                        m.getLote().getId(),
                        m.getLote().getCodigo(),
                        m.getLote().getFechaVencimiento(),
                        m.getCantidad()))
            .toList());
  }
}
