package cl.duoc.sonidovivo_backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String email;

    // En el login no se valida el largo: el mensaje de error es genérico a propósito.
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
