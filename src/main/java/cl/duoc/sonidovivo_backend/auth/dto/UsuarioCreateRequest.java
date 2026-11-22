package cl.duoc.sonidovivo_backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioCreateRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
    private String apellido;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    @Pattern(regexp = "(?i)^[^@\\s]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)$",
            message = "El correo debe ser @duoc.cl, @profesor.duoc.cl o @gmail.com")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 4, max = 64, message = "La contraseña debe tener entre 4 y 64 caracteres")
    private String password;

    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    private String numeroTelefono;

    @NotBlank(message = "El rol es obligatorio")
    private String rol;
}