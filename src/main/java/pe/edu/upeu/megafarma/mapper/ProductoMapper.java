package pe.edu.upeu.megafarma.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.megafarma.dto.*;
import pe.edu.upeu.megafarma.entity.Producto;

@Component
public class ProductoMapper {
  public ProductoResponseDTO toResponse(Producto p) {
    return new ProductoResponseDTO(
        p.getId(),
        p.getCodigo(),
        p.getNombre(),
        p.getDescripcion(),
        p.getPrincipioActivo(),
        p.getPresentacion(),
        p.getLaboratorio(),
        p.getPrecioVenta(),
        p.getStockMinimo(),
        p.isActivo(),
        p.getCreadoEn());
  }
}
