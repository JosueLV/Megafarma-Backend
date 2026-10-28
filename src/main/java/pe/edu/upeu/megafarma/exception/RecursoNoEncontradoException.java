package pe.edu.upeu.megafarma.exception;

import org.springframework.http.HttpStatus;

public class RecursoNoEncontradoException extends ReglaNegocioException {
  public RecursoNoEncontradoException(String mensaje) {
    super("NO_ENCONTRADO", mensaje, HttpStatus.NOT_FOUND);
  }
}
