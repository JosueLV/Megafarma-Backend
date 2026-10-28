package pe.edu.upeu.megafarma.exception;

import org.springframework.http.HttpStatus;

public class ReglaNegocioException extends RuntimeException {
  public final String codigo;
  public final HttpStatus estado;

  public ReglaNegocioException(String codigo, String mensaje) {
    this(codigo, mensaje, HttpStatus.CONFLICT);
  }

  public ReglaNegocioException(String codigo, String mensaje, HttpStatus estado) {
    super(mensaje);
    this.codigo = codigo;
    this.estado = estado;
  }
}
