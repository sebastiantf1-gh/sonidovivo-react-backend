package cl.duoc.sonidovivo_backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioUpdateRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
    private String apellido;

    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    private String numeroTelefono;

    // Opcional: si el Administrador lo envía, actualiza el rol del usuario.
    @Size(max = 50, message = "El rol no puede superar 50 caracteres")
    private String rol;
}