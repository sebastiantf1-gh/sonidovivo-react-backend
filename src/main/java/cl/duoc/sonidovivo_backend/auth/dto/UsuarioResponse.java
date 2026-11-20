package cl.duoc.sonidovivo_backend.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// Nunca incluye la contraseña ni su hash.
@Data
@Builder
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String numeroTelefono;
    private String rol;
    private LocalDateTime fechaRegistro;
}
