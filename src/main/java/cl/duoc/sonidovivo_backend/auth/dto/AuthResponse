package cl.duoc.sonidovivo_backend.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType;   // siempre "Bearer"
    private long expiresIn;     // segundos
    private UsuarioResponse usuario;
}
