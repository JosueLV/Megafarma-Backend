package pe.edu.upeu.megafarma.dto.auth;

import jakarta.validation.constraints.*;

public record LoginRequestDTO(
    @NotBlank @Size(max = 60) String username, @NotBlank @Size(max = 72) String password) {}
