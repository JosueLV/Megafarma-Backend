package pe.edu.upeu.megafarma.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pe.edu.upeu.megafarma.exception.dto.ErrorResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ReglaNegocioException.class)
  ResponseEntity<ErrorResponseDTO> negocio(ReglaNegocioException e, HttpServletRequest r) {
    return error(e.estado.value(), e.codigo, e.getMessage(), r, Map.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponseDTO> validacion(
      MethodArgumentNotValidException e, HttpServletRequest r) {
    Map<String, String> campos = new LinkedHashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
    return error(400, "VALIDACION", "Revisa los campos indicados", r, campos);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    MissingRequestHeaderException.class,
    MissingServletRequestParameterException.class,
    HandlerMethodValidationException.class
  })
  ResponseEntity<ErrorResponseDTO> malformado(Exception e, HttpServletRequest r) {
    return error(
        400,
        "SOLICITUD_INVALIDA",
        "Revisa el JSON, fechas, parámetros y cabeceras obligatorias",
        r,
        Map.of());
  }

  @ExceptionHandler(AuthenticationException.class)
  ResponseEntity<ErrorResponseDTO> autenticacion(Exception e, HttpServletRequest r) {
    return error(401, "CREDENCIALES_INVALIDAS", "Usuario o contraseña incorrectos", r, Map.of());
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ErrorResponseDTO> permisos(Exception e, HttpServletRequest r) {
    return error(403, "SIN_PERMISO", "No tienes permiso para esta operación", r, Map.of());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ErrorResponseDTO> integridad(Exception e, HttpServletRequest r) {
    return error(
        409, "CONFLICTO_DATOS", "El registro entra en conflicto con datos existentes", r, Map.of());
  }

  @ExceptionHandler({ConcurrencyFailureException.class})
  ResponseEntity<ErrorResponseDTO> concurrencia(Exception e, HttpServletRequest r) {
    return error(
        409,
        "CONFLICTO_CONCURRENCIA",
        "Otra operación está modificando los datos. Reintenta con la misma clave",
        r,
        Map.of());
  }

  @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
  ResponseEntity<ErrorResponseDTO> noExiste(Exception e, HttpServletRequest r) {
    return error(404, "NO_ENCONTRADO", "Recurso no encontrado", r, Map.of());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  ResponseEntity<ErrorResponseDTO> metodo(Exception e, HttpServletRequest r) {
    return error(405, "METODO_NO_PERMITIDO", "Método no permitido", r, Map.of());
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  ResponseEntity<ErrorResponseDTO> contenido(Exception e, HttpServletRequest r) {
    return error(415, "CONTENIDO_NO_SOPORTADO", "Usa Content-Type: application/json", r, Map.of());
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponseDTO> inesperado(Exception e, HttpServletRequest r) {
    log.error("Error no controlado en {}", r.getRequestURI(), e);
    return error(500, "ERROR_INTERNO", "No se pudo completar la operación", r, Map.of());
  }

  private ResponseEntity<ErrorResponseDTO> error(
      int status, String codigo, String mensaje, HttpServletRequest r, Map<String, String> campos) {
    return ResponseEntity.status(status)
        .body(
            new ErrorResponseDTO(
                Instant.now(), status, codigo, mensaje, r.getRequestURI(), campos));
  }
}
