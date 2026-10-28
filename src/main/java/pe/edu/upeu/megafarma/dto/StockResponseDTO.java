package pe.edu.upeu.megafarma.dto;

public record StockResponseDTO(
    Long productoId,
    String codigo,
    String nombre,
    long stockFisico,
    long stockVendible,
    long stockNoVendible,
    long stockMinimo,
    boolean bajoMinimo) {}
