package pe.edu.upeu.megafarma.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.springframework.stereotype.Component;
import pe.edu.upeu.megafarma.exception.dto.ErrorResponseDTO;

@Component
public class RespuestaErrorSeguridad {
  private final ObjectMapper json;

  public RespuestaErrorSeguridad(ObjectMapper json) {
    this.json = json;
  }

  public void escribir(
      HttpServletRequest req, HttpServletResponse res, int status, String codigo, String mensaje)
      throws IOException {
    res.setStatus(status);
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    json.writeValue(
        res.getOutputStream(),
        new ErrorResponseDTO(
            Instant.now(), status, codigo, mensaje, req.getRequestURI(), Map.of()));
  }
}
